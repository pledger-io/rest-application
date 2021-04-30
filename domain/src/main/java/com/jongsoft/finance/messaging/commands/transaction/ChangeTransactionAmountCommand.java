package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;

import java.math.BigDecimal;

public record ChangeTransactionAmountCommand(long id, BigDecimal amount, String currency) implements ApplicationEvent {
}
