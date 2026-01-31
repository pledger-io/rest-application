package com.jongsoft.finance.suggestion.adapter.api;

import com.jongsoft.finance.suggestion.domain.model.TransactionRule;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface TransactionRuleProvider {

    Sequence<TransactionRule> lookup();

    Optional<TransactionRule> lookup(long id);

    Sequence<TransactionRule> lookup(String group);

    @Deprecated
    void save(TransactionRule rule);
}
