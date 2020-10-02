package com.jongsoft.finance.domain.transaction;

import com.jongsoft.lang.control.Optional;
import io.reactivex.Flowable;

public interface TransactionRuleGroupProvider {

    Flowable<TransactionRuleGroup> lookup();
    Optional<TransactionRuleGroup> lookup(String name);

}
