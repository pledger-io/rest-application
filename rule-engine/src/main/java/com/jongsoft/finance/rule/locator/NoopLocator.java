package com.jongsoft.finance.rule.locator;

import com.jongsoft.finance.core.RuleColumn;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
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
