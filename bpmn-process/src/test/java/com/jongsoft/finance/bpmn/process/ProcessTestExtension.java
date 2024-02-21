package com.jongsoft.finance.bpmn.process;

import com.jongsoft.finance.messaging.EventBus;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.function.Consumer;

public class ProcessTestExtension extends MicronautJunit5Extension {

    public interface ProcessExecution<T extends ProcessExecution> {
        ProcessExecution<?> obtainChildProcess(String processKey);
        T verifyCompleted();
        <Y> T yankVariable(String variableName, Consumer<Y> consumer);
    }

    @Override
    protected void beforeEach(ExtensionContext context, @Nullable Object testInstance, @Nullable AnnotatedElement method, List<Property> propertyAnnotations) {
        super.beforeEach(context, testInstance, method, propertyAnnotations);

        new EventBus(applicationContext.getBean(ApplicationEventPublisher.class));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return isProcessContext(parameterContext)
                || super.supportsParameter(parameterContext, extensionContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (isProcessContext(parameterContext)) {
            return new RuntimeContext(applicationContext);
        }
        return super.resolveParameter(parameterContext, extensionContext);
    }

    private static boolean isProcessContext(ParameterContext parameterContext) {
        return parameterContext.getParameter()
                .getType()
                .isAssignableFrom(RuntimeContext.class);
    }
}
