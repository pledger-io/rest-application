package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

/** Command to complete the savings goal and close it in the system. */
public record CompleteSavingGoalCommand(long id) implements ApplicationEvent {

    public static void savingGoalCompleted(long id) {
        new CompleteSavingGoalCommand(id).publish();
    }
}
