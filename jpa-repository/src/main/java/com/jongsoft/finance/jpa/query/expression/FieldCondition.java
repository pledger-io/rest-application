package com.jongsoft.finance.jpa.query.expression;

import com.jongsoft.finance.jpa.query.BooleanExpression;

import jakarta.persistence.Query;

import java.util.function.Supplier;

record FieldCondition(
        String tableAlias, String fieldId, String field, FieldEquation equation, Object value)
        implements BooleanExpression {
    @Override
    public String hqlExpression() {
        var actualAlias = tableAlias != null ? tableAlias + "." : "";
        var comparator =
                switch (equation) {
                    case EQ -> "=";
                    case GTE -> ">=";
                    case LTE -> "<=";
                    case LT -> "<";
                    case IN, NIN -> "";
                    case LIKE -> "LIKE";
                };

        Supplier<String> fieldFunc = () -> "%s%s".formatted(actualAlias, field);
        if (value instanceof String && equation == FieldEquation.LIKE) {
            fieldFunc = () -> "lower(%s%s)".formatted(actualAlias, field);
        }

        if (equation == FieldEquation.IN) {
            return " %s IN(:%s) ".formatted(fieldFunc.get(), fieldId);
        } else if (equation == FieldEquation.NIN) {
            return " %s NOT IN(:%s) ".formatted(fieldFunc.get(), fieldId);
        }

        return " %s %s :%s ".formatted(fieldFunc.get(), comparator, fieldId);
    }

    @Override
    public void addParameters(Query query) {
        query.setParameter(fieldId, value);
    }

    @Override
    public BooleanExpression cloneWithAlias(String alias) {
        return new FieldCondition(alias, fieldId, field, equation, value);
    }
}
