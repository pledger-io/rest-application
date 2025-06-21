package com.jongsoft.finance.jpa.query;

import com.jongsoft.finance.jpa.query.expression.Expressions;
import jakarta.persistence.Query;

public class SubQuery extends BaseQuery<SubQuery> implements BooleanExpression {

  private final String parentAlias;
  private String from;
  private String projection;

  SubQuery(String parentAlias, String tableAlias) {
    super(tableAlias);
    this.projection = "1";
    this.from = "";
    this.parentAlias = parentAlias;
  }

  public SubQuery from(String field) {
    if (parentAlias != null) {
      this.from = parentAlias + ".";
    }
    this.from += field;
    return this;
  }

  public SubQuery from(Class<?> entityType) {
    this.from = entityType.getSimpleName();
    return this;
  }

  public SubQuery fieldEqParentField(String field, String parentField) {
    var actualParent = parentAlias == null ? "e." : parentAlias + ".";
    condition(Expressions.equals(
        Expressions.field(tableAlias() + "." + field),
        Expressions.field(actualParent + parentField)));
    return this;
  }

  public SubQuery project(String projection) {
    this.projection = projection;
    return this;
  }

  @Override
  public String hqlExpression() {
    if (conditions().isEmpty()) {
      throw new IllegalStateException("Cannot create a sub selection without filters.");
    }

    StringBuilder hql = new StringBuilder("(SELECT ")
        .append(projection)
        .append(" FROM ")
        .append(from)
        .append(" ")
        .append(tableAlias())
        .append(" WHERE ");
    hql.append(" 1=1 ");
    for (var condition : conditions()) {
      hql.append(" AND ").append(condition.hqlExpression());
    }
    hql.append(")");
    return hql.toString();
  }

  @Override
  public void addParameters(Query query) {
    for (var condition : conditions()) {
      condition.addParameters(query);
    }
  }

  @Override
  public BooleanExpression cloneWithAlias(String alias) {
    var subQuery = new SubQuery(alias, tableAlias());
    subQuery.projection = this.projection;
    subQuery.from(from);
    conditions().forEach(condition -> subQuery.conditions().add(condition));
    return subQuery;
  }
}
