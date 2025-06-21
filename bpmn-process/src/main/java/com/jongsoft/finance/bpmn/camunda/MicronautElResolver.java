package com.jongsoft.finance.bpmn.camunda;

import com.jongsoft.finance.core.JavaBean;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Optional;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.impl.juel.jakarta.el.ELContext;
import org.camunda.bpm.impl.juel.jakarta.el.ELResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This ELResolver implementation allows to resolve beans from the Micronaut application-context.
 */
public class MicronautElResolver extends ELResolver {

  private static final Argument<JavaBean> TYPE = Argument.of(JavaBean.class);

  private static final Logger log = LoggerFactory.getLogger(MicronautElResolver.class);

  protected final ApplicationContext applicationContext;

  public MicronautElResolver(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    if (base == null) {
      log.debug("Looking up bean '{}' in Micronaut application-context", property);
      var resolvedBean = getBeanForKey(property.toString());
      if (resolvedBean.isPresent()) {
        context.setPropertyResolved(true);
        return resolvedBean.get();
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
    if (base == null
        && !applicationContext.containsBean(TYPE, Qualifiers.byName(property.toString()))) {
      throw new ProcessEngineException("Cannot set value of '"
          + property
          + "', it resolves to a bean defined in the Micronaut"
          + " application-context.");
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

  private Optional<?> getBeanForKey(String key) {
    if (applicationContext.containsBean(TYPE, Qualifiers.byName(key))) {
      return Optional.of(applicationContext.getBean(TYPE, Qualifiers.byName(key)));
    }

    return Optional.empty();
  }
}
