package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.messaging.ApplicationEvent;

import java.math.BigDecimal;

public record ChangeTransactionAmountCommand(long id, BigDecimal amount, String currency)
        implements ApplicationEvent {

    public static void amountChanged(long id, BigDecimal amount, String currency) {
        new ChangeTransactionAmountCommand(id, amount, currency).publish();
    }
}
