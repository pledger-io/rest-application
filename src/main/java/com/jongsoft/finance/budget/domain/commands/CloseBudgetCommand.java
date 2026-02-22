package com.jongsoft.finance.budget.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.LocalDate;

public record CloseBudgetCommand(long id, LocalDate end) implements ApplicationEvent {

    public static void budgetClosed(long id, LocalDate end) {
        new CloseBudgetCommand(id, end).publish();
    }
}
