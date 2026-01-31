package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;

/**
 * An installment event on a saving goal indicates that money should be reserved (allocated). This
 * would increase the allocated amount of money for the saving goal.
 */
public record RegisterSavingInstallmentCommand(long id, BigDecimal amount)
        implements ApplicationEvent {

    public static void savingInstallmentRegistered(long id, BigDecimal amount) {
        new RegisterSavingInstallmentCommand(id, amount).publish();
    }
}
