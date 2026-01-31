package com.jongsoft.finance.budget.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;

public record UpdateExpenseCommand(long id, BigDecimal amount) implements ApplicationEvent {

    public static void expenseUpdated(long id, BigDecimal amount) {
        new UpdateExpenseCommand(id, amount).publish();
    }
}
