package com.jongsoft.finance.bpmn.process;

import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.domain.FinTrack;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mockito;

import java.util.function.Consumer;

public class ProcessTestExtension implements BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, ParameterResolver {

    public interface ProcessExecution<T extends ProcessExecution> {
        ProcessExecution<?> obtainChildProcess(String processKey);
        HistoricProcessExecution verifyCompleted();
        <Y> T yankVariable(String variableName, Consumer<Y> consumer);
    }

    private RuntimeContext runtimeContext;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        var applicationBean = Mockito.spy(new FinTrack(Mockito.mock(Encoder.class)));

        var store = extensionContext.getRoot()
                .getStore(ExtensionContext.Namespace.create(MicronautJunit5Extension.class));
        var applicationContext = store.get(ApplicationContext.class, ApplicationContext.class);
        applicationContext.registerSingleton(FinTrack.class, applicationBean);

        runtimeContext = new RuntimeContext(applicationContext);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        runtimeContext.clean();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        runtimeContext.resetMocks();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return isProcessContext(parameterContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (isProcessContext(parameterContext)) {
            return runtimeContext;
        }

        return null;
    }

    private static boolean isProcessContext(ParameterContext parameterContext) {
        return parameterContext.getParameter()
                .getType()
                .isAssignableFrom(RuntimeContext.class);
    }
}
