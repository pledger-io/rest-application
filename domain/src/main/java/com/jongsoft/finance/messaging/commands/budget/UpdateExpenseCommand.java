package com.jongsoft.finance.messaging.commands.budget;

import com.jongsoft.finance.messaging.ApplicationEvent;

import java.math.BigDecimal;

public record UpdateExpenseCommand(long id, BigDecimal amount) implements ApplicationEvent {

    public static void expenseUpdated(long id, BigDecimal amount) {
        new UpdateExpenseCommand(id, amount).publish();
    }
}
