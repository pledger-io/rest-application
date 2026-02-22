package com.jongsoft.finance.suggestion.adapter.api;

import com.jongsoft.finance.suggestion.domain.model.TransactionRuleGroup;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface TransactionRuleGroupProvider {

    Sequence<TransactionRuleGroup> lookup();

    Optional<TransactionRuleGroup> lookup(String name);
}
