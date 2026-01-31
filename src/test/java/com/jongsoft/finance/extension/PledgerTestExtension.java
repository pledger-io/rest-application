package com.jongsoft.finance.extension;

import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.adapter.api.Encoder;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.extension.*;

import static org.mockito.Mockito.mock;

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

        applicationContext.registerSingleton(CurrentUserProvider.class, mock(CurrentUserProvider.class));
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

        getStore(context)
              .put(PledgerContext.class, new PledgerContext(applicationContext));
        getStore(context)
              .put(PledgerRequests.class, new PledgerRequests(applicationContext.getBean(RequestSpecification.class)));
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
