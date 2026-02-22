package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;

public record ChangeTransactionAmountCommand(long id, BigDecimal amount, String currency)
        implements ApplicationEvent {

    public static void amountChanged(long id, BigDecimal amount, String currency) {
        new ChangeTransactionAmountCommand(id, amount, currency).publish();
    }
}
