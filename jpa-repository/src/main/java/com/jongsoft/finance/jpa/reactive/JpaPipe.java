package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.lang.Control;
import com.jongsoft.lang.control.Optional;
import io.micronaut.data.model.Sort;

import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

abstract class JpaPipe<T, R extends JpaPipe<T, R>> {

    private String hql;
    private Optional<Integer> limit;
    private Optional<Integer> offset;
    private Optional<Sort> sort;

    private final Map<String, Object> parameters;

    JpaPipe() {
        this.parameters = new HashMap<>();
        this.limit = Control.Option();
        this.offset = Control.Option();
        this.sort = Control.Option();
    }

    /**
     * Set a parameter to be replaced in the {@link #hql(java.lang.String)} operation. Note that
     * this will replace any parameter with the same name.
     *
     * @param parameter the name of the parameter
     * @param value the value to be substituted
     * @return this instance
     */
    public <Y> R set(String parameter, Y value) {
        parameters.put(parameter, value);
        return self();
    }

    public R setAll(Map<String, ?> parameters) {
        parameters.forEach(this.parameters::put);
        return self();
    }

    /**
     * Set the HQL query that will be used in the pipeline.
     *
     * @param hql the query
     * @return this instance
     */
    public R hql(String hql) {
        this.hql = hql;
        return self();
    }

    /**
     * Set the limit that will be applied by the pipeline in the resultset.
     *
     * @param limit the limit
     * @return this instance
     */
    public R limit(int limit) {
        this.limit = Control.Option(limit);
        return self();
    }

    /**
     * Set the offset that will be used in the pipeline's resultset. This is the number of
     * records that should be skipped before returning the first result.
     *
     * @param limit the limit
     * @return this instance
     */
    public R offset(int offset) {
        this.offset = Control.Option(offset);
        return self();
    }

    /**
     * Set the sorting of the results.
     *
     * @param sort the actual sort to be applied
     * @return this instance
     */
    public R sort(Sort sort) {
        this.sort = Control.Option(sort);
        return self();
    }

    String hql() {
        return this.hql;
    }

    void applyParameters(Query query) {
        parameters.forEach(query::setParameter);
    }

    void applyPaging(Query query) {
        this.offset.ifPresent(query::setFirstResult);
        this.limit.ifPresent(query::setMaxResults);
    }

    String sort() {
        if (!sort.isPresent()) {
            return "";
        }

        var sorting = new StringBuilder(" order by ");
        for (var iterator = sort.get().getOrderBy().iterator(); iterator.hasNext(); ) {
            var order = iterator.next();
            sorting.append(order.getProperty())
                    .append(" ")
                    .append(order.isAscending() ? "asc" : "desc");
            if (iterator.hasNext()) {
                sorting.append(", ");
            }
        }
        return sorting.toString();
    }

    int limit() {
        return limit.getOrSupply(() -> Integer.MAX_VALUE);
    }

    protected abstract R self();
}
