package com.jongsoft.finance.core.domain.jpa.query;

import jakarta.persistence.Query;

public interface BooleanExpression {

    default BooleanExpression cloneWithAlias(String alias) {
        return this;
    }

    default String tableAlias() {
        return null;
    }

    String hqlExpression();

    default void addParameters(Query query) {}
}
