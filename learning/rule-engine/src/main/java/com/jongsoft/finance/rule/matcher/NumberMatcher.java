package com.jongsoft.finance.rule.matcher;

import com.jongsoft.finance.core.RuleOperation;

import java.util.function.Supplier;

public class NumberMatcher implements ConditionMatcher {

    private Supplier<Boolean> innerMatcher;

    @Override
    public ConditionMatcher prepare(RuleOperation operation, String expectation, Object actual) {
        var checkAmount = Double.parseDouble(expectation);
        var castedActual = (Double) actual;
        if (castedActual == null) {
            innerMatcher = () -> false;
            return this;
        }

        innerMatcher = switch (operation) {
            case LESS_THAN -> () -> castedActual < checkAmount;
            case MORE_THAN -> () -> castedActual > checkAmount;
            case EQUALS -> () -> castedActual == checkAmount;
            default -> () -> false;
        };

        return this;
    }

    @Override
    public boolean matches() {
        return innerMatcher.get();
    }

}
