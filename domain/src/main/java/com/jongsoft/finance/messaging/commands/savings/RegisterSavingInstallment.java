package com.jongsoft.finance.messaging.commands.savings;

import com.jongsoft.finance.core.ApplicationEvent;

import java.math.BigDecimal;

/**
 * An installment event on a saving goal indicates that money should be reserved (allocated). This would increase the
 * allocated amount of money for the saving goal.
 */
public record RegisterSavingInstallment(long id, BigDecimal amount) implements ApplicationEvent {
}
