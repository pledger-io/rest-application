package com.jongsoft.finance.banking.types;

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
