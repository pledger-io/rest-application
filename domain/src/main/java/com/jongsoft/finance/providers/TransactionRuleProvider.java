package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import io.reactivex.Flowable;

public interface TransactionRuleProvider extends DataProvider<TransactionRule>, Exportable<TransactionRule> {

    Flowable<TransactionRule> lookup(String group);

    @Deprecated
    void save(TransactionRule rule);

    @Override
    default boolean supports(Class<TransactionRule> supportingClass) {
        return TransactionRule.class.equals(supportingClass);
    }
}
