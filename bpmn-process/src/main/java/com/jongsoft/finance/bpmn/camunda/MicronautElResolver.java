package com.jongsoft.finance.bpmn.camunda;

import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.impl.juel.jakarta.el.ELContext;
import org.camunda.bpm.impl.juel.jakarta.el.ELResolver;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

/**
 * This ELResolver implementation allows to resolve beans from the Micronaut application-context.
 */
public class MicronautElResolver extends ELResolver {

    protected final ApplicationContext applicationContext;

    public MicronautElResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (base == null) {
            // according to javadoc, can only be a String
            var key = (String) property;

            var qualifier = Qualifiers.byName(key);
            if (applicationContext.containsBean(Object.class, qualifier)) {
                context.setPropertyResolved(true);
                return applicationContext.getBean(Object.class, qualifier);
            }
        }

        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        if (base == null) {
            var key = (String) property;
            if (applicationContext.containsBean(Object.class, Qualifiers.byName(key))) {
                throw new ProcessEngineException("Cannot set value of '" + property +
                        "', it resolves to a bean defined in the Micronaut application-context.");
            }
        }
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object arg) {
        return Object.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object arg) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object arg1, Object arg2) {
        return Object.class;
    }

}
