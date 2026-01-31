package com.jongsoft.finance.suggestion.domain.service.rule;

import com.jongsoft.finance.suggestion.types.RuleOperation;

/** A matcher that can be used to compare a value against an expectation. */
public interface ConditionMatcher {

    /**
     * Prepare the matcher for execution.
     *
     * @param operation The operation to perform.
     * @param expectation The expectation to compare against.
     * @param actual The actual value to compare.
     * @return The matcher.
     */
    ConditionMatcher prepare(RuleOperation operation, String expectation, Object actual);

    /**
     * Execute the matcher.
     *
     * @return True if the matcher matches, false otherwise.
     */
    boolean matches();
}
