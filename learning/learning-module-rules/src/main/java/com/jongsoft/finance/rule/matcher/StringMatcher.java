package com.jongsoft.finance.rule.matcher;

import com.jongsoft.finance.core.RuleOperation;

import java.util.function.Supplier;

public class StringMatcher implements ConditionMatcher {

    private Supplier<Boolean> innerMatcher;

    @Override
    public ConditionMatcher prepare(RuleOperation operation, String expectation, Object actual) {
        boolean nonNull = expectation != null && actual != null;

        innerMatcher = () -> false;
        if (nonNull) {
            var castedActual = actual.toString().toLowerCase();
            var loweredExpectation = expectation.toLowerCase();

            innerMatcher =
                    switch (operation) {
                        case EQUALS -> () -> loweredExpectation.equals(castedActual);
                        case CONTAINS -> () -> castedActual.contains(loweredExpectation);
                        case STARTS_WITH -> () -> castedActual.startsWith(loweredExpectation);
                        default -> () -> false;
                    };
        }

        return this;
    }

    @Override
    public boolean matches() {
        return innerMatcher.get();
    }
}
