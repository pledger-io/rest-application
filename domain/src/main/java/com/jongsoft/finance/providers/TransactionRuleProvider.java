package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.lang.collection.Sequence;

public interface TransactionRuleProvider extends DataProvider<TransactionRule>, Exportable<TransactionRule> {

    Sequence<TransactionRule> lookup(String group);

    @Deprecated
    void save(TransactionRule rule);

    @Override
    default boolean supports(Class<TransactionRule> supportingClass) {
        return TransactionRule.class.equals(supportingClass);
    }
}
