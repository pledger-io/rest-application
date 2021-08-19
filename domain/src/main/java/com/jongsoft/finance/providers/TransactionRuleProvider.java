package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import reactor.core.publisher.Flux;

public interface TransactionRuleProvider extends DataProvider<TransactionRule>, Exportable<TransactionRule> {

    Flux<TransactionRule> lookup(String group);

    @Deprecated
    void save(TransactionRule rule);

    @Override
    default boolean supports(Class<TransactionRule> supportingClass) {
        return TransactionRule.class.equals(supportingClass);
    }
}
