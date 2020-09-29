package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.lang.API;
import com.jongsoft.lang.control.Optional;

import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

abstract class JpaPipe<T, R extends JpaPipe<T, R>> {

    private String hql;
    private Optional<Integer> limit;
    private Optional<Integer> offset;

    private final Map<String, Object> parameters;

    JpaPipe() {
        this.parameters = new HashMap<>();
        this.limit = API.Option();
        this.offset = API.Option();
    }

    public <Y> R set(String parameter, Y value) {
        parameters.put(parameter, value);
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

    protected abstract R self();
}
