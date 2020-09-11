package com.jongsoft.finance.domain.transaction;

import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface TransactionRuleGroupProvider {

    Sequence<TransactionRuleGroup> lookup();
    Optional<TransactionRuleGroup> lookup(String name);

}
