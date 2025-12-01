package com.jongsoft.finance.core;

public enum SystemAccountTypes {
    RECONCILE,
    LOAN,
    DEBT,
    MORTGAGE,
    DEBTOR,
    CREDITOR;

    public String label() {
        return this.name().toLowerCase();
    }
}
