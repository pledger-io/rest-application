package com.jongsoft.finance.bpmn.camunda;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.naming.NameResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.scripting.engine.Resolver;
import org.camunda.bpm.engine.impl.scripting.engine.ResolverFactory;

/** Resolves beans from the Micronaut application context. */
@Slf4j
public class MicronautBeanResolver implements ResolverFactory, Resolver {

  private final ApplicationContext applicationContext;

  public MicronautBeanResolver(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public boolean containsKey(Object key) {
    log.debug("Looking up key {} in {}", key, getKeySet());
    return key instanceof String && getKeySet().contains(key);
  }

  @Override
  public Object get(Object key) {
    if (key instanceof String) {
      log.debug("Looking up bean {} in {}", key, getKeySet());
      Qualifier<Object> qualifier = Qualifiers.byName((String) key);
      if (applicationContext.containsBean(Object.class, qualifier)) {
        return applicationContext.getBean(Object.class, qualifier);
      }
    }
    return null;
  }

  @Override
  public Set<String> keySet() {
    return getKeySet();
  }

  @Override
  public Resolver createResolver(VariableScope variableScope) {
    log.debug("Creating resolver for {}", variableScope);
    return this;
  }

  protected synchronized Set<String> getKeySet() {
    return applicationContext.getAllBeanDefinitions().stream()
        .filter(beanDefinition -> !beanDefinition.getClass().getName().startsWith("io.micronaut."))
        .map(this::getBeanName)
        .collect(Collectors.toSet());
  }

  protected String getBeanName(BeanDefinition<?> beanDefinition) {
    var beanQualifier =
        beanDefinition
            .getAnnotationMetadata()
            .findDeclaredAnnotation(AnnotationUtil.NAMED)
            .flatMap(AnnotationValue::stringValue);
    return beanQualifier.orElseGet(
        () -> {
          if (beanDefinition instanceof NameResolver resolver) {
            return resolver.resolveName().orElse(getBeanNameFromType(beanDefinition));
          }
          return getBeanNameFromType(beanDefinition);
        });
  }

  protected String getBeanNameFromType(BeanDefinition<?> beanDefinition) {
    String beanName = beanDefinition.getBeanType().getSimpleName();
    // lower the first character
    return Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
  }
}
