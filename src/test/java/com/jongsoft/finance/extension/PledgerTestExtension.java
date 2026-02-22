package com.jongsoft.finance.extension;

import com.jongsoft.finance.core.adapter.api.Encoder;

import io.micronaut.context.ApplicationContext;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import io.restassured.specification.RequestSpecification;

import org.junit.jupiter.api.extension.*;

import java.util.function.Function;

public class PledgerTestExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(PledgerTestExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter()
              .getType()
              .isAssignableFrom(PledgerContext.class)
              || parameterContext.getParameter()
              .getType()
              .isAssignableFrom(PledgerRequests.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().isAssignableFrom(PledgerContext.class)) {
            return getStore(extensionContext)
                  .get(PledgerContext.class, PledgerContext.class);
        }
        if (parameterContext.getParameter().getType().isAssignableFrom(PledgerRequests.class)) {
            return getStore(extensionContext)
                  .get(PledgerRequests.class, PledgerRequests.class);
        }

        throw new ParameterResolutionException("Unsupported parameter type: " + parameterContext.getParameter().getType());
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        var store = context.getRoot()
              .getStore(ExtensionContext.Namespace.create(MicronautJunit5Extension.class));
        var applicationContext = store.get(ApplicationContext.class, ApplicationContext.class);

        applicationContext.registerSingleton(Encoder.class, new Encoder() {
            @Override
            public String encrypt(String value) {
                return value;
            }

            @Override
            public boolean matches(String encoded, String value) {
                return encoded.equals(value);
            }
        });

        Function<String, String> bearerTokenProvider = (username) -> applicationContext.getBean(AccessRefreshTokenGenerator.class)
            .generate(Authentication.build(username))
            .get()
            .getAccessToken();

        getStore(context)
              .put(PledgerContext.class, new PledgerContext(applicationContext));
        getStore(context)
              .put(PledgerRequests.class, new PledgerRequests(applicationContext.getBean(RequestSpecification.class), bearerTokenProvider));
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        getStore(extensionContext)
              .get(PledgerContext.class, PledgerContext.class).reset();
    }

    private static ExtensionContext.Store getStore(ExtensionContext extensionContext) {
        return extensionContext.getRoot().getStore(NAMESPACE);
    }
}
