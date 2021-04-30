package com.jongsoft.finance.messaging.commands.budget;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.user.Budget;

public record CreateBudgetCommand(Budget budget) implements ApplicationEvent {
}
