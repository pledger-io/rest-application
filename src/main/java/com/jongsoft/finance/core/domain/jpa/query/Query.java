package com.jongsoft.finance.core.domain.jpa.query;

import java.util.function.Consumer;

public interface Query<Q extends Query<Q>> {

    /**
     * Adds a condition to the query where the specified field is equal to the provided condition.
     *
     * @param field the field to apply the equality condition
     * @param condition the condition value to match
     * @return the updated SimpleQuery instance with the specified equality condition added
     */
    <C> Q fieldEq(String field, C condition);

    /**
     * Retrieves a query instance with a conditional check where the specified field's value is
     * similar to the provided condition.
     *
     * @param field the field to apply the similarity condition on
     * @param condition the condition value to match for similarity
     * @return the updated query instance with the field similarity condition applied
     */
    Q fieldLike(String field, String condition);

    /**
     * Retrieves a query instance with a conditional check where the specified field's value falls
     * between the provided start and end values.
     *
     * @param <C> the type of the start and end values
     * @param field the field to apply the range condition on
     * @param start the starting value of the range
     * @param end the ending value of the range
     * @return the updated query instance with the field range condition applied
     */
    <C> Q fieldBetween(String field, C start, C end);

    /**
     * Adds a condition to the query where the specified field is equal to the provided conditions.
     *
     * @param field the field to apply the equality condition
     * @param conditions the condition values to match
     * @return the updated instance of the query with the specified equality conditions added
     */
    <C> Q fieldEqOneOf(String field, C... conditions);

    /**
     * Adds a condition to the query where the specified field is not equal to any of the provided
     * conditions.
     *
     * @param field the field to apply the not equals condition
     * @param conditions the condition values to be checked for inequality
     * @return the updated instance of the query with the specified not equals conditions added
     */
    <C> Q fieldNotEqOneOf(String field, C... conditions);

    /**
     * Adds a condition to the query where the specified field is greater than or equal to the
     * provided condition.
     *
     * @param field the field to apply the greater than or equal condition
     * @param condition the condition value to match
     * @return the updated SimpleQuery instance with the specified greater than or equal condition
     *     added
     */
    <C> Q fieldGtOrEq(String field, C condition);

    /**
     * Adds a condition to the query where the specified field is greater than or equal to the
     * provided condition. Null-values are also allowed.
     *
     * @param field the field to apply the greater than or equal condition
     * @param condition the condition value to match
     * @return the updated SimpleQuery instance with the specified greater than or equal condition
     *     added
     */
    <C> Q fieldGtOrEqNullable(String field, C condition);

    /**
     * Retrieves a query instance with a conditional check where the specified field's value is less
     * than or equal to the provided condition.
     *
     * @param field the field to apply the less than or equal condition on
     * @param condition the condition value to match for the less than or equal comparison
     * @return the updated query instance with the field less than or equal condition applied
     */
    <C> Q fieldLtOrEq(String field, C condition);

    /**
     * Adds a condition to the query where the specified field is null.
     *
     * @param field the field to check for null value
     * @return the updated query instance with the specified null check condition added
     */
    Q fieldNull(String field);

    /**
     * Adds a condition to the query based on the given BooleanExpression.
     *
     * @param expression the BooleanExpression to be evaluated for adding a condition to the query
     * @return the updated query instance with the specified condition added
     */
    Q condition(BooleanExpression expression);

    /**
     * Executes the given subQueryBuilder only if it exists in the context of the current query.
     *
     * @param subQueryBuilder the consumer function defining the sub-query to be checked for
     *     existence
     * @return the updated query instance with the existence check condition applied
     */
    Q whereExists(Consumer<SubQuery> subQueryBuilder);

    /**
     * Executes the given subQueryBuilder only if it does not exist in the context of the current
     * query.
     *
     * @param subQueryBuilder the consumer function defining the sub-query to be checked for
     *     non-existence
     * @return the updated query instance with the non-existence check condition applied
     */
    Q whereNotExists(Consumer<SubQuery> subQueryBuilder);
}
