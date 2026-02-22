package com.jongsoft.finance.core.domain.jpa.query.expression;

import com.jongsoft.finance.core.domain.jpa.query.BooleanExpression;

record FieldNull(String tableAlias, String field) implements BooleanExpression {
    @Override
    public String hqlExpression() {
        var actualAlias = tableAlias != null ? tableAlias + "." : "";
        return " %s%s IS NULL ".formatted(actualAlias, field);
    }

    @Override
    public BooleanExpression cloneWithAlias(String alias) {
        return new FieldNull(alias, field);
    }
}
