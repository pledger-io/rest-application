package com.jongsoft.finance.core.domain.jpa.query;

import com.jongsoft.lang.Control;

public abstract class JpaFilterBuilder<E> {

    protected Integer skipRows;
    protected Integer limitRows;
    protected String orderBy;
    protected boolean orderAscending;
    private final BaseQuery<?> query;

    public JpaFilterBuilder() {
        this.query = new BaseQuery(null) {};
    }

    protected Query<?> query() {
        return query;
    }

    /**
     * Applies the conditions from the current query to the provided query for filtering.
     *
     * @param applyTo the query to apply the conditions to
     */
    public void applyTo(Query<?> applyTo) {
        query.conditions().forEach(condition -> {
            if (condition.tableAlias() == null && applyTo instanceof JpaQuery<?> jpaQuery) {
                applyTo.condition(condition.cloneWithAlias("e"));
            } else {
                applyTo.condition(condition);
            }
        });
        if (applyTo instanceof JpaQuery<?> jpaQuery) {
            applyPagingOnly(jpaQuery);
        }
    }

    /**
     * Applies pagination settings to the provided JpaQuery object.
     *
     * @param applyTo the JpaQuery object to apply pagination settings to
     */
    public void applyPagingOnly(JpaQuery<?> applyTo) {
        Control.Option(skipRows).ifPresent(applyTo::skip);
        Control.Option(limitRows).ifPresent(applyTo::limit);
        applyTo.orderBy(orderBy, orderAscending);
    }

    public abstract void user(String username);

    public abstract Class<E> entityType();
}
