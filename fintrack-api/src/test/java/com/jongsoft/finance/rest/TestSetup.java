package com.jongsoft.finance.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Currency;

@MicronautTest(environments = {"no-camunda", "test"})
public class TestSetup {

    protected final UserIdentifier ACTIVE_USER_IDENTIFIER = new UserIdentifier("test-user");
    protected final UserAccount ACTIVE_USER = Mockito.spy(UserAccount.builder()
            .id(1L)
            .username(ACTIVE_USER_IDENTIFIER)
            .password("$2a$10$mgkjpf4nZqCLqAzFCjv5F.2Sj1b8k7yFJZVM0MZ4J9dJKzgBYPKDi")
            .theme("dark")
            .primaryCurrency(Currency.getInstance("EUR"))
            .secret(new DefaultSecretGenerator().generate())
            .roles(Collections.List(new Role(AuthenticationRoles.IS_ADMIN), new Role(AuthenticationRoles.IS_AUTHENTICATED)))
            .build());

    @Inject
    protected UserProvider userProvider;
    @Inject
    protected FilterFactory filterFactory;

    @BeforeEach
    void initialize() {
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType("application/json")
                // override the default object mapper to use the JavaTimeModule
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
                .addHeader("Authorization", "Basic dGVzdC11c2VyOjEyMzQ=")
                .build();

        Mockito.when(userProvider.lookup(ACTIVE_USER.getUsername())).thenReturn(Control.Option(ACTIVE_USER));

        // initialize the event bus
        new EventBus(Mockito.mock(ApplicationEventPublisher.class));
    }

    @AfterEach
    void after() {
        Mockito.reset(userProvider, filterFactory);
    }

    @MockBean
    UserProvider userProvider() {
        return Mockito.mock(UserProvider.class);
    }

    @MockBean
    CurrencyProvider currencyProvider() {
        return Mockito.mock(CurrencyProvider.class);
    }

    @MockBean
    SettingProvider settingProvider() {
        return Mockito.mock(SettingProvider.class);
    }

    @MockBean
    @Replaces
    FilterFactory generateFilterMock() {
        final FilterFactory filterFactory = Mockito.mock(FilterFactory.class);
        Mockito.when(filterFactory.transaction())
                .thenReturn(Mockito.mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.account())
                .thenReturn(Mockito.mock(AccountProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.expense())
                .thenReturn(Mockito.mock(ExpenseProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.category())
                .thenReturn(Mockito.mock(CategoryProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.tag())
                .thenReturn(Mockito.mock(TagProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.schedule())
                .thenReturn(Mockito.mock(TransactionScheduleProvider.FilterCommand.class, InvocationOnMock::getMock));
        return filterFactory;
    }
}
