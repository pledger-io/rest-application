package com.jongsoft.finance.rule.matcher;

import java.util.function.Supplier;

import com.jongsoft.finance.core.RuleOperation;

public class NumberMatcher implements ConditionMatcher {

    private Supplier<Boolean> innerMatcher;

    @Override
    public ConditionMatcher prepare(RuleOperation operation, String expectation, Object actual) {
        var checkAmount = Double.parseDouble(expectation);
        var castedActual = (Double) actual;

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
