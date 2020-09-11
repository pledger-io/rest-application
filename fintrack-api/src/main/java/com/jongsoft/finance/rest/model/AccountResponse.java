package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.account.Account;

public class AccountResponse {

    private final Account wrapped;

    public AccountResponse(final Account wrapped) {
        this.wrapped = wrapped;
    }
}
