package com.jongsoft.finance.rule.matcher;

import com.jongsoft.finance.core.RuleOperation;

public interface ConditionMatcher {

    ConditionMatcher prepare(RuleOperation operation, String expectation, Object actual);

    boolean matches();

}
