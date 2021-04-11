package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.providers.AccountProvider;

public class AccountSpendingImpl implements AccountProvider.AccountSpending {

    private final Account account;
    private final double total;
    private final double average;

    public AccountSpendingImpl(Account account, double total, double average) {
        this.account = account;
        this.total = total;
        this.average = average;
    }

    @Override
    public Account account() {
        return account;
    }

    @Override
    public double total() {
        return total;
    }

    @Override
    public double average() {
        return average;
    }
}
