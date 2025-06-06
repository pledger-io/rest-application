package com.jongsoft.finance.jpa.query.expression;

import com.jongsoft.finance.jpa.query.BooleanExpression;
import jakarta.persistence.Query;

record FieldBetween<T>(String tableAlias, String fieldId, String field, T start, T end)
    implements BooleanExpression {
  @Override
  public String hqlExpression() {
    var actualAlias = tableAlias != null ? tableAlias + "." : "";
    return " %s%s BETWEEN :%s_start AND :%s_end ".formatted(actualAlias, field, fieldId, fieldId);
  }

  @Override
  public void addParameters(Query query) {
    query.setParameter("%s_start".formatted(fieldId), start);
    query.setParameter("%s_end".formatted(fieldId), end);
  }

  @Override
  public BooleanExpression cloneWithAlias(String alias) {
    return new FieldBetween<>(alias, fieldId, field, start, end);
  }
}
