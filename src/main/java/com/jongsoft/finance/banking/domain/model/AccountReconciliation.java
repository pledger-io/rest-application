package com.jongsoft.finance.banking.domain.model;

public record AccountReconciliation(
        int year, double startBalance, double endBalance, double computedStartBalance) {}
