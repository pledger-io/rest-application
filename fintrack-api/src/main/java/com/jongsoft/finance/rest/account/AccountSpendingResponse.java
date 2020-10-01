package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.rest.model.AccountResponse;

public class AccountSpendingResponse {

    private final AccountProvider.AccountSpending wrapped;

    public AccountSpendingResponse(AccountProvider.AccountSpending wrapped) {
        this.wrapped = wrapped;
    }

    public AccountResponse getAccount() {
        return new AccountResponse(wrapped.account());
    }

    public double getTotal() {
        return wrapped.total();
    }

    public double getAverage() {
        return wrapped.average();
    }

}
