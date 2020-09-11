package com.jongsoft.finance.rule;

import com.jongsoft.finance.domain.transaction.TransactionRule;

public interface RuleEngine {

    RuleDataSet run(RuleDataSet input);
    RuleDataSet run(RuleDataSet input, TransactionRule rule);

}
