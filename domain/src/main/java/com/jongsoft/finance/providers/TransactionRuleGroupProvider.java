package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface TransactionRuleGroupProvider {

    Sequence<TransactionRuleGroup> lookup();

    Optional<TransactionRuleGroup> lookup(String name);
}
