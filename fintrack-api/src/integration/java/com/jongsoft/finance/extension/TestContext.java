package com.jongsoft.finance.extension;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.lang.Control;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import io.micronaut.context.ApplicationContext;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

public class TestContext {

    private final static Logger log = LoggerFactory.getLogger(TestContext.class);

    public record Server(String baseUri, int port) {
    }

    private String authenticatedWith;
    private String authenticationToken;
    private final ApplicationContext applicationContext;

    public TestContext(Server server, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(server.baseUri)
                .setPort(server.port)
                .setBasePath("/api")
                .setContentType("application/json")
                .setAccept("application/json")
                .setConfig(RestAssured.config()
                        .objectMapperConfig(
                                ObjectMapperConfig.objectMapperConfig()
                                        .jackson2ObjectMapperFactory(
                                                (type, s) -> {
                                                    var mapper = new ObjectMapper();
                                                    var module = new SimpleModule();
                                                    module.addSerializer(LocalDate.class, new JsonSerializer<>() {
                                                        @Override
                                                        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                                                            gen.writeString(value.toString());
                                                        }
                                                    });
                                                    mapper.registerModule(module);
                                                    mapper.setVisibility(mapper.getVisibilityChecker()
                                                            .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
                                                    return mapper;
                                                }
                                        ))
                        .logConfig(RestAssured.config().getLogConfig()
                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                .build();
    }

    public TestContext register(String username, String password) {
        log.info("Creating account for user: {}", username);
        RestAssured.given()
                .body("""
                        {
                            "username": "%s",
                            "password": "%s"
                        }""".formatted(username, password))
                .put("/security/create-account")
            .then()
                .statusCode(201);
        return this;
    }

    public TestContext authenticate(String username, String password) {
        log.info("Authenticating user: {}", username);
        var jsonPath = RestAssured.given()
                .body("""
                        {
                            "username": "%s",
                            "password": "%s"
                        }""".formatted(username, password))
                .post("/security/authenticate")
            .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath();

        authenticationToken = jsonPath.getString("access_token");
        authenticatedWith = username;
        return this;
    }

    public TestContext multiFactor() {
        authenticationToken = authRequest()
            .given()
                .contentType("application/json")
                .body("""
                        {
                          "verificationCode": "%s"
                        }""".formatted(generateToken()))
            .when()
                .post("/security/2-factor")
            .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getString("access_token");

        return this;
    }

    public TestContext enableMFA() {
        authRequest()
            .given()
                .contentType("application/json")
                .body("""
                        {
                          "verificationCode": "%s"
                        }""".formatted(generateToken()))
            .when()
                .post("/profile/multi-factor/enable")
            .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        return this;
    }

    public ProfileContext profile() {
        return new ProfileContext(this::authRequest);
    }

    public AccountContext accounts() {
        return new AccountContext(authRequest());
    }

    public TransactionContext transactions() {
        return new TransactionContext(this::authRequest);
    }

    public BudgetContext budgets() {
        return new BudgetContext(this::authRequest);
    }

    public String upload(InputStream inputStream) {
        return authRequest()
                .contentType("multipart/form-data")
                .multiPart("upload", "account1.svg", inputStream)
                .post("/attachment")
            .then()
                .statusCode(201)
                .extract()
                .body()
                .jsonPath()
                .getString("fileCode");
    }

    public RequestSpecification authRequest() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + authenticationToken);
    }

    private String generateToken() {
        var optionalUser = applicationContext.getBean(UserProvider.class)
                .lookup(new UserIdentifier(authenticatedWith));
        Assertions.assertThat(optionalUser.isPresent()).isTrue();

        return optionalUser.map(UserAccount::getSecret)
                .map(secret -> Control.Try(() -> new DefaultCodeGenerator()
                                .generate(secret, Math.floorDiv(new SystemTimeProvider().getTime(), 30)))
                        .get())
                .get();
    }
}
