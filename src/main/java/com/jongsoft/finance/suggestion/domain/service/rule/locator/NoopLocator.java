package com.jongsoft.finance.suggestion.domain.service.rule.locator;

import com.jongsoft.finance.suggestion.domain.service.rule.ChangeLocator;
import com.jongsoft.finance.suggestion.types.RuleColumn;

import jakarta.inject.Singleton;

@Singleton
class NoopLocator implements ChangeLocator {

    @Override
    public Object locate(RuleColumn column, String change) {
        return change;
    }

    @Override
    public boolean supports(RuleColumn column) {
        return RuleColumn.TAGS.equals(column);
    }
}
