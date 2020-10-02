package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;

public class TransactionRuleGroupResponse {

    private final TransactionRuleGroup wrapped;

    public TransactionRuleGroupResponse(TransactionRuleGroup wrapped) {
        this.wrapped = wrapped;
    }

    public String getName() {
        return wrapped.getName();
    }

    public int getSort() {
        return wrapped.getSort();
    }

}
