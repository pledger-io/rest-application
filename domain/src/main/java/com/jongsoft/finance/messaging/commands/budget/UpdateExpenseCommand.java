package com.jongsoft.finance.messaging.commands.budget;

import com.jongsoft.finance.core.ApplicationEvent;

import java.math.BigDecimal;

public record UpdateExpenseCommand(long id, BigDecimal amount)
        implements ApplicationEvent {
}
