package com.jongsoft.finance.jpa.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Query;

import com.jongsoft.finance.jpa.FilterDelegate;

public abstract class FilterCommandJpa implements FilterDelegate<FilterCommandJpa> {

    private final Map<String, Object> parameters;
    private final Map<String, String> filters;

    protected FilterCommandJpa() {
        this.parameters = new HashMap<>();
        this.filters = new HashMap<>();
    }

    @Override
    public String generateHql() {
        var hqlBuilder = new StringBuilder(fromHql());
        filters.values().forEach(hqlFilter -> hqlBuilder
                .append(System.lineSeparator())
                .append(hqlFilter));

        return hqlBuilder.toString();
    }

    @Override
    public FilterCommandJpa prepareQuery(Query query) {
        parameters.forEach(query::setParameter);
        return this;
    }

    protected abstract String fromHql();

    protected void hql(String key, String hql) {
        filters.put(key, hql);
    }

    protected void parameter(String key, Object value) {
        parameters.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FilterCommandJpa other) {
            return parameters.equals(other.parameters) &&
                    filters.equals(other.filters);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, filters);
    }
}
