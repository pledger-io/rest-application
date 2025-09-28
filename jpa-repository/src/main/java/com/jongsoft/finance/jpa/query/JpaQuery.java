package com.jongsoft.finance.jpa.query;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.jpa.ResultPageImpl;
import com.jongsoft.finance.jpa.query.expression.Expressions;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.control.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a simple query class that assists in building and executing queries for a specified
 * entity type. Allows adding conditions and join fetch directives to the query.
 *
 * @param <E> the type of the entity for which the query is being constructed
 */
public class JpaQuery<E> extends BaseQuery<JpaQuery<E>> {

  private record JoinStatement(String field, boolean fetch) {}

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Class<E> table;
  private final EntityManager entityManager;
  private final List<JoinStatement> joinTables = new ArrayList<>();
  private final List<ComputationExpression> groupings = new ArrayList<>();

  private Integer skipRows;
  private Integer limitRows;
  private String orderBy;
  private boolean orderAscending;

  JpaQuery(EntityManager entityManager, Class<E> entityType) {
    super("e");
    this.entityManager = entityManager;
    table = entityType;
  }

  /**
   * Sets the number of rows to skip in the query result.
   *
   * @param numberOfRows the number of rows to skip
   * @return the updated JpaQuery instance with the specified number of rows skipped
   */
  public JpaQuery<E> skip(int numberOfRows) {
    skipRows = numberOfRows;
    return this;
  }

  /**
   * Sets the limit for the number of rows to be retrieved in the query result.
   *
   * @param numberOfRows the number of rows to limit the query result to
   * @return the updated JpaQuery instance with the specified row limit
   */
  public JpaQuery<E> limit(int numberOfRows) {
    limitRows = numberOfRows;
    return this;
  }

  /**
   * Sets the field to order the query result by and specifies whether the ordering should be
   * ascending or descending.
   *
   * @param field the field to order by
   * @param ascending boolean value indicating whether to order in ascending order
   * @return the updated JpaQuery instance with the specified ordering
   */
  public JpaQuery<E> orderBy(String field, boolean ascending) {
    orderBy = field;
    orderAscending = ascending;
    return this;
  }

  /**
   * Adds the specified fields to the list of groupings for the query result.
   *
   * @param fields the fields to group by
   * @return the updated JpaQuery instance with the added groupings
   */
  public JpaQuery<E> groupBy(String... fields) {
    for (var field : fields) {
      groupings.add(Expressions.field("e." + field));
    }
    return this;
  }

  /**
   * Adds the specified computation expressions to the list of groupings for the query result.
   *
   * @param expressions the computation expressions to group by
   * @return the updated JpaQuery instance with the added groupings
   */
  public JpaQuery<E> groupBy(ComputationExpression... expressions) {
    groupings.addAll(Arrays.asList(expressions));
    return this;
  }

  /**
   * Adds a join fetch for a specific field to the query.
   *
   * @param field the field to join fetch
   * @return the updated SimpleQuery instance with the specified field join fetched
   */
  public JpaQuery<E> joinFetch(String field) {
    joinTables.add(new JoinStatement(field, true));
    return this;
  }

  /**
   * Joins the specified field in the query.
   *
   * @param field the field to join
   * @return the updated JpaQuery instance with the specified field joined
   */
  public JpaQuery<E> join(String field) {
    joinTables.add(new JoinStatement(field, false));
    return this;
  }

  /**
   * Projects a specific type of projection using the provided projection type and expression.
   *
   * @param <C> the type of the projection result
   * @param projectionType the class representing the type of projection result
   * @param projection the expression for the projection
   * @return an Optional containing the projected result if successful, otherwise an empty
   *     Optional
   */
  public <C> Optional<C> projectSingleValue(Class<C> projectionType, String projection) {
    var hql = "SELECT %s %s".formatted(projection, generateHql(true));

    return Control.Try(() -> (C) createQuery(projectionType, hql).getSingleResult())
        .map(Control::Option)
        .recover(
            e -> {
              logger.warn("Unable to find projection, cause: {}", e.getLocalizedMessage());
              return Control.Option();
            })
        .get();
  }

  /**
   * Projects a specific type of projection using the provided projection type and expression.
   *
   * @param <C> the type of the projection result
   * @param projectionType the class representing the type of projection result
   * @param projection the expression for the projection
   * @return a Stream of the projected results based on the specified projection type and
   *     expression
   */
  public <C> Stream<C> project(Class<C> projectionType, String projection) {
    var hql = "SELECT %s %s".formatted(projection, generateHql(true));
    return createQuery(projectionType, hql).getResultStream();
  }

  /**
   * Create and execute a stream query based on the specified conditions.
   *
   * @return a Stream of the elements resulting from the query execution
   */
  public Stream<E> stream() {
    var hql = "SELECT DISTINCT e %s".formatted(generateHql(true));
    return createQuery(table, hql).getResultStream();
  }

  /**
   * Retrieves a single result based on the specified query conditions. If multiple results are
   * found, it returns an empty Optional. If no results are found, it logs a trace and returns an
   * empty Optional.
   *
   * @return an Optional containing the single result if found, otherwise an empty Optional
   */
  public Optional<E> singleResult() {
    var hql = "SELECT DISTINCT e %s".formatted(generateHql(true));

    return Control.Try(() -> (E) createQuery(table, hql).getSingleResult())
        .map(Control::Option)
        .recover(
            e -> {
              logger.trace("Unable to find entity, cause: {}", e.getLocalizedMessage());
              return Control.Option();
            })
        .get();
  }

  /**
   * Retrieves a paginated result page based on the specified query conditions.
   *
   * @return a ResultPage containing elements resulting from the query execution paginated based
   *     on the provided conditions
   */
  public ResultPage<E> paged() {
    var countHql = "SELECT count(DISTINCT e.id) %s".formatted(generateHql(false));
    var query = entityManager.createQuery(countHql, Long.class);
    for (var condition : conditions()) {
      condition.addParameters(query);
    }

    var numberOfRecords = query.getSingleResult();
    var limit = limitRows == null ? Integer.MAX_VALUE : limitRows;
    if (numberOfRecords > 0) {
      // only run the actual query if we had hits in the count query.
      return new ResultPageImpl<E>(
          stream().collect(ReactiveEntityManager.sequenceCollector()), limit, numberOfRecords);
    }

    return new ResultPageImpl<E>(Collections.List(), limit, 0);
  }

  private <X> TypedQuery<X> createQuery(Class<X> resultType, String hql) {
    logger.trace("Running query, with base table {}: {}", table.getSimpleName(), hql);
    var query = entityManager.createQuery(hql, resultType);
    for (var condition : conditions()) {
      condition.addParameters(query);
    }

    if (skipRows != null) {
      query.setFirstResult(skipRows);
    }
    if (limitRows != null) {
      query.setMaxResults(limitRows);
    }

    return query;
  }

  private String generateHql(boolean withModifiers) {
    if (conditions().isEmpty()) {
      logger.warn("Query ran without any filters against {}.", table.getSimpleName());
    }

    StringBuilder hql = new StringBuilder("FROM %s e".formatted(table.getSimpleName()));
    for (var joinTable : joinTables) {
      hql.append(" JOIN ");
      if (joinTable.fetch()) {
        hql.append("FETCH ");
      }
      hql.append("e.%s".formatted(joinTable.field()));
    }

    hql.append(" WHERE 1=1 ");
    for (var condition : conditions()) {
      hql.append(" AND ").append(condition.hqlExpression());
    }

    if (withModifiers) {
      if (!groupings.isEmpty()) {
        hql.append(" GROUP BY ");
        for (var field : groupings) {
          hql.append(field.hqlExpression()).append(",");
        }
        hql.deleteCharAt(hql.length() - 1);
      }

      if (orderBy != null) {
        hql.append(" ORDER BY e.%s %s".formatted(orderBy, orderAscending ? "ASC" : "DESC"));
      }
    }

    return hql.toString();
  }
}
