package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.domain.model.Account;

import java.math.BigDecimal;

public class AccountSpendingImpl implements AccountProvider.AccountSpending {

    private final Account account;
    private final BigDecimal total;
    private final double average;

    public AccountSpendingImpl(Account account, BigDecimal total, double average) {
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
        return total.doubleValue();
    }

    @Override
    public double average() {
        return average;
    }
}
