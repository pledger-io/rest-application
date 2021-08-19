package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.lang.control.Optional;
import reactor.core.publisher.Flux;

public interface TransactionRuleGroupProvider {

    Flux<TransactionRuleGroup> lookup();
    Optional<TransactionRuleGroup> lookup(String name);

}
