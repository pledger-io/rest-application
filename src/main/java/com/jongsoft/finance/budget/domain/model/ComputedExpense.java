package com.jongsoft.finance.budget.domain.model;

import java.math.BigDecimal;

public record ComputedExpense(
        Budget.Expense expense,
        BigDecimal amountLeft,
        BigDecimal amountSpent,
        BigDecimal dailyLeft,
        BigDecimal dailySpent) {}
