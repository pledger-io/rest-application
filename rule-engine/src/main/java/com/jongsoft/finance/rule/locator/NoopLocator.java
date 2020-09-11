package com.jongsoft.finance.rule.locator;

import javax.inject.Singleton;

import com.jongsoft.finance.core.RuleColumn;

@Singleton
public class NoopLocator implements ChangeLocator {

    @Override
    public Object locate(RuleColumn column, String change) {
        return change;
    }

    @Override
    public boolean supports(RuleColumn column) {
        return RuleColumn.TAGS.equals(column);
    }

}
