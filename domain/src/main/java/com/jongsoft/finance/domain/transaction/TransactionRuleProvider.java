package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.domain.core.DataProvider;
import com.jongsoft.finance.domain.core.Exportable;
import com.jongsoft.lang.collection.Sequence;

public interface TransactionRuleProvider extends DataProvider<TransactionRule>, Exportable<TransactionRule> {

    Sequence<TransactionRule> lookup(String group);

    @Deprecated
    TransactionRule save(TransactionRule rule);

    @Override
    default boolean supports(Class<TransactionRule> supportingClass) {
        return TransactionRule.class.equals(supportingClass);
    }
}
