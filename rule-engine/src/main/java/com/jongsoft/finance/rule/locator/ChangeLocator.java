package com.jongsoft.finance.rule.locator;

import com.jongsoft.finance.core.RuleColumn;

public interface ChangeLocator {

    Object locate(RuleColumn column, String change);

    boolean supports(RuleColumn column);

}
