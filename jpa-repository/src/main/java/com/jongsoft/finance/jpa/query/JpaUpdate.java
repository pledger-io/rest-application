package com.jongsoft.finance.jpa.query;

import com.jongsoft.finance.jpa.query.expression.Expressions;
import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaUpdate<E> extends BaseQuery<JpaUpdate<E>> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final EntityManager entityManager;
  private final Class<E> entityType;
  private final Map<String, ComputationExpression> updateFields;

  JpaUpdate(EntityManager entityManager, Class<E> entityType) {
    super(null);
    this.entityManager = entityManager;
    this.entityType = entityType;
    updateFields = new HashMap<>();
  }

  public <V> JpaUpdate<E> set(String field, V value) {
    updateFields.put(field, Expressions.value(value));
    return this;
  }

  public <V> JpaUpdate<E> set(String field, ComputationExpression computation) {
    updateFields.put(field, computation);
    return this;
  }

  public void execute() {
    var hql = new StringBuilder("update %s set ".formatted(entityType.getSimpleName()));
    for (var x = 0; x < updateFields.size(); x++) {
      if (x > 0) {
        hql.append(", ");
      }
      var field = updateFields.keySet().toArray()[x];
      hql.append("%s = %s".formatted(field, updateFields.get(field).hqlExpression()));
    }
    hql.append(" where 1=1 ");
    for (var condition : conditions()) {
      hql.append(" AND ").append(condition.hqlExpression());
    }

    logger.trace("Running update on {}: {}", entityType.getSimpleName(), hql);

    var query = entityManager.createQuery(hql.toString());
    updateFields.values().forEach(expression -> expression.addParameters(query));
    conditions().forEach(condition -> condition.addParameters(query));

    query.executeUpdate();
  }
}
