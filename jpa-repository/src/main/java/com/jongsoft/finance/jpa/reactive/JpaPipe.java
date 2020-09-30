package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.lang.API;
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
        this.limit = API.Option();
        this.offset = API.Option();
        this.sort = API.Option();
    }

    public <Y> R set(String parameter, Y value) {
        parameters.put(parameter, value);
        return self();
    }

    public R setAll(Map<String, ?> parameters) {
        parameters.forEach(this.parameters::put);
        return self();
    }

    public R hql(String hql) {
        this.hql = hql;
        return self();
    }

    public R limit(int limit) {
        this.limit = API.Option(limit);
        return self();
    }

    public R offset(int offset) {
        this.offset = API.Option(offset);
        return self();
    }

    public R sort(Sort sort) {
        this.sort = API.Option(sort);
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
