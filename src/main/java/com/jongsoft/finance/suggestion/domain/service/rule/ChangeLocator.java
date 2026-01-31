package com.jongsoft.finance.suggestion.domain.service.rule;

import com.jongsoft.finance.suggestion.types.RuleColumn;

/** Locates the entity that will be used to update the rule data set. */
public interface ChangeLocator {

    /**
     * Locates the entity that will be used to update the rule data set.
     *
     * @param column The column that is being updated.
     * @param change The change that is being applied.
     * @return The object that will be used to update the rule data set.
     */
    Object locate(RuleColumn column, String change);

    /**
     * Determines if this locator supports the given column.
     *
     * @param column The column to check.
     * @return True if this locator supports the given column.
     */
    boolean supports(RuleColumn column);
}
