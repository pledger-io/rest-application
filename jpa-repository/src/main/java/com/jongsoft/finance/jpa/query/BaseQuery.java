package com.jongsoft.finance.jpa.query;

import com.jongsoft.finance.jpa.query.expression.Expressions;
import com.jongsoft.finance.jpa.query.expression.FieldEquation;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseQuery<Q extends Query<Q>> implements Query<Q> {
  protected record InnerQuery(String fieldCondition, SubQuery subQuery)
      implements BooleanExpression {
    @Override
    public String hqlExpression() {
      return fieldCondition + subQuery.hqlExpression();
    }

    @Override
    public void addParameters(jakarta.persistence.Query query) {
      subQuery.addParameters(query);
    }

    @Override
    public BooleanExpression cloneWithAlias(String alias) {
      return new InnerQuery(fieldCondition, (SubQuery) subQuery.cloneWithAlias(alias));
    }
  }

  private final List<BooleanExpression> conditions;
  private final String tableAlias;

  protected BaseQuery(String tableAlias) {
    this.conditions = new ArrayList<>();
    this.tableAlias = tableAlias;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> Q fieldEq(String field, C condition) {
    conditions.add(Expressions.fieldCondition(tableAlias, field, FieldEquation.EQ, condition));
    return (Q) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Q fieldLike(String field, String condition) {
    conditions.add(Expressions.fieldLike(tableAlias, field, condition));
    return (Q) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> Q fieldBetween(String field, C start, C end) {
    conditions.add(Expressions.fieldBetween(tableAlias, field, start, end));
    return (Q) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> Q fieldEqOneOf(String field, C... conditions) {
    if (conditions.length == 1) {
      return fieldEq(field, conditions[0]);
    } else if (conditions.length == 2) {
      this.conditions.add(Expressions.or(
          Expressions.fieldCondition(tableAlias, field, FieldEquation.EQ, conditions[0]),
          Expressions.fieldCondition(tableAlias, field, FieldEquation.EQ, conditions[1])));
    } else {
      this.conditions.add(Expressions.fieldCondition(
          tableAlias, field, FieldEquation.IN, Arrays.asList(conditions)));
    }

    return (Q) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> Q fieldNotEqOneOf(String field, C... conditions) {
    this.conditions.add(Expressions.fieldCondition(
        tableAlias, field, FieldEquation.NIN, Arrays.asList(conditions)));
    return (Q) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> Q fieldGtOrEq(String field, C condition) {
    conditions.add(Expressions.fieldCondition(tableAlias, field, FieldEquation.GTE, condition));
    return (Q) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> Q fieldGtOrEqNullable(String field, C condition) {
    conditions.add(Expressions.or(
        Expressions.fieldCondition(tableAlias, field, FieldEquation.GTE, condition),
        Expressions.fieldNull(tableAlias, field)));
    return (Q) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> Q fieldLtOrEq(String field, C condition) {
    conditions.add(Expressions.fieldCondition(tableAlias, field, FieldEquation.LTE, condition));
    return (Q) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Q fieldNull(String field) {
    conditions.add(Expressions.fieldNull(tableAlias, field));
    return (Q) this;
  }

  @SuppressWarnings("unchecked")
  public Q fieldIn(String field, Class<?> subQueryEntity, Consumer<SubQuery> subQueryBuilder) {
    var alias = tableAlias == null ? "" : tableAlias + ".";

    var qubQuery = new SubQuery(tableAlias, BaseQuery.generateRandomString()).from(subQueryEntity);
    subQueryBuilder.accept(qubQuery);
    conditions.add(new InnerQuery(alias + field + " IN ", qubQuery));
    return (Q) this;
  }

  @Override
  public Q whereExists(Consumer<SubQuery> subQueryBuilder) {
    var subQuery = new SubQuery(tableAlias, BaseQuery.generateRandomString());
    subQueryBuilder.accept(subQuery);
    conditions.add(new InnerQuery("EXISTS", subQuery));
    return (Q) this;
  }

  @Override
  public Q whereNotExists(Consumer<SubQuery> subQueryBuilder) {
    var subQuery = new SubQuery(tableAlias, BaseQuery.generateRandomString());
    subQueryBuilder.accept(subQuery);
    conditions.add(new InnerQuery("NOT EXISTS", subQuery));
    return (Q) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Q condition(BooleanExpression expression) {
    conditions.add(expression);
    return (Q) this;
  }

  protected List<BooleanExpression> conditions() {
    return conditions;
  }

  public String tableAlias() {
    return tableAlias;
  }

  static String generateRandomString() {
    var random = new SecureRandom();
    var sb = new StringBuilder(5);
    var allowedChard = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    for (int i = 0; i < 12; i++) {
      int randomIndex = random.nextInt(allowedChard.length());
      sb.append(allowedChard.charAt(randomIndex));
    }

    return sb.toString();
  }
}
