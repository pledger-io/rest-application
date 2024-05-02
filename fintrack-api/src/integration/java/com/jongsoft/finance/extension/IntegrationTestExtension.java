package com.jongsoft.finance.extension;

import com.jongsoft.lang.Control;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextLifeCycle;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTestExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback {

    private final static Logger log = LoggerFactory.getLogger(IntegrationTestExtension.class);

    private ApplicationContext applicationContext;
    private TestContext testContext;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter()
                .getType()
                .isAssignableFrom(TestContext.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return testContext;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        applicationContext = ApplicationContext.run("test");
        Control.Option(applicationContext.getBean(EmbeddedApplication.class))
                .ifPresent(ApplicationContextLifeCycle::start);
        Control.Option(applicationContext.getBean(EmbeddedServer.class))
                .ifPresent(server -> {
                    testContext = new TestContext(new TestContext.Server(
                            server.getScheme() + "://" + server.getHost(),
                            server.getPort()
                    ));
                });
    }

    @Override
    public void afterAll(ExtensionContext context) {
        applicationContext.close();
    }
}
