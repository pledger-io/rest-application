package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Flowable;

public interface TransactionRuleGroupProvider {

    Flowable<TransactionRuleGroup> lookup();
    Optional<TransactionRuleGroup> lookup(String name);

}
