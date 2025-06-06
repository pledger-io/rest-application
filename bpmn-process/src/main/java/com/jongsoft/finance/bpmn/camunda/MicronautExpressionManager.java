package com.jongsoft.finance.bpmn.camunda;

import java.time.LocalDate;
import org.camunda.bpm.engine.impl.el.JuelExpressionManager;
import org.camunda.bpm.impl.juel.jakarta.el.CompositeELResolver;
import org.camunda.bpm.impl.juel.jakarta.el.ELResolver;

public class MicronautExpressionManager extends JuelExpressionManager {

  private final MicronautElResolver micronautElResolver;

  public MicronautExpressionManager(MicronautElResolver micronautElResolver) {
    this.micronautElResolver = micronautElResolver;

    try {
      addFunction("math:double", Double.class.getMethod("parseDouble", String.class));
      addFunction("date:parse", LocalDate.class.getMethod("parse", CharSequence.class));
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected ELResolver createElResolver() {
    var resolver = super.createElResolver();
    if (resolver instanceof CompositeELResolver e) {
      e.add(micronautElResolver);
    }

    return resolver;
  }
}
