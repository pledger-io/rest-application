package com.jongsoft.finance.jpa.query.expression;

import com.jongsoft.finance.jpa.query.BooleanExpression;
import jakarta.persistence.Query;

public record OrExpression(BooleanExpression left, BooleanExpression right) implements BooleanExpression {
    @Override
    public String hqlExpression() {
        return "(%s OR %s)".formatted(left.hqlExpression(), right.hqlExpression());
    }

    @Override
    public void addParameters(Query query) {
        left.addParameters(query);
        right.addParameters(query);
    }

    @Override
    public BooleanExpression cloneWithAlias(String alias) {
        return new OrExpression(left.cloneWithAlias(alias), right.cloneWithAlias(alias));
    }
}
