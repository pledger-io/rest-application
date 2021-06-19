package com.jongsoft.finance.messaging.commands.savings;

import com.jongsoft.finance.core.ApplicationEvent;

/**
 * Command to complete the savings goal and close it in the system.
 */
public record CompleteSavingGoalCommand(long id) implements ApplicationEvent {
}
