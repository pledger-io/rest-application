package com.jongsoft.finance.messaging.commands.savings;

import com.jongsoft.finance.messaging.ApplicationEvent;

/**
 * Command to complete the savings goal and close it in the system.
 */
public record CompleteSavingGoalCommand(long id) implements ApplicationEvent {

    public static void savingGoalCompleted(long id) {
        new CompleteSavingGoalCommand(id)
                .publish();
    }
}
