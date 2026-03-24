package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;

public record UpdateTaxBracketCommand(long id, String name, BigDecimal rate)
        implements ApplicationEvent {
    public static void taxBracketUpdated(long id, String name, BigDecimal rate) {
        new UpdateTaxBracketCommand(id, name, rate).publish();
    }
}
