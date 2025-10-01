package com.jongsoft.finance.rest.statistic;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
class BalanceResponse {

    private double balance;

    public BalanceResponse(final double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }
}
