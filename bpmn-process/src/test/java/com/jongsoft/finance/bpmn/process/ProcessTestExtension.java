package com.jongsoft.finance.bpmn.process;

import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.domain.FinTrack;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.mockito.Mockito;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.function.Consumer;

public class ProcessTestExtension extends MicronautJunit5Extension {

    public interface ProcessExecution<T extends ProcessExecution> {
        ProcessExecution<?> obtainChildProcess(String processKey);
        HistoricProcessExecution verifyCompleted();
        <Y> T yankVariable(String variableName, Consumer<Y> consumer);
    }

    private RuntimeContext runtimeContext;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        super.beforeAll(extensionContext);

        var applicationBean = Mockito.spy(new FinTrack(Mockito.mock(Encoder.class)));
        applicationContext.registerSingleton(FinTrack.class, applicationBean);

        runtimeContext = new RuntimeContext(applicationContext);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        runtimeContext.clean();
        super.afterAll(extensionContext);
    }

    @Override
    protected void beforeEach(ExtensionContext context, @Nullable Object testInstance, @Nullable AnnotatedElement method, List<Property> propertyAnnotations) {
        super.beforeEach(context, testInstance, method, propertyAnnotations);
        runtimeContext.resetMocks();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return isProcessContext(parameterContext)
                || super.supportsParameter(parameterContext, extensionContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (isProcessContext(parameterContext)) {
            return runtimeContext;
        }
        return super.resolveParameter(parameterContext, extensionContext);
    }

    private static boolean isProcessContext(ParameterContext parameterContext) {
        return parameterContext.getParameter()
                .getType()
                .isAssignableFrom(RuntimeContext.class);
    }
}
