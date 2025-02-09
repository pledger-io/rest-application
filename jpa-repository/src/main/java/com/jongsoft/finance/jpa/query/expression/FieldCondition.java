package com.jongsoft.finance.jpa.query.expression;

import com.jongsoft.finance.jpa.query.BooleanExpression;
import jakarta.persistence.Query;

record FieldCondition(String tableAlias, String fieldId, String field, FieldEquation equation, Object value) implements BooleanExpression {
    @Override
    public String hqlExpression() {
        var actualAlias = tableAlias != null ? tableAlias + "." : "";
        var comparator = switch (equation) {
            case EQ -> "=";
            case GTE -> ">=";
            case LTE -> "<=";
            case LT -> "<";
            case IN, NIN -> "";
            case LIKE -> "LIKE";
        };

        if (equation == FieldEquation.IN) {
            return " %s%s IN(:%s) ".formatted(actualAlias, field, fieldId);
        } else if (equation == FieldEquation.NIN) {
            return " %s%s NOT IN(:%s) ".formatted(actualAlias, field, fieldId);

        }

        return " %s%s %s :%s ".formatted(actualAlias, field, comparator, fieldId);
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
