package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;

public record CreateTaxBracketCommand(String name, BigDecimal rate) implements ApplicationEvent {
    public static void taxBracketCreated(String name, BigDecimal rate) {
        new CreateTaxBracketCommand(name, rate).publish();
    }
}
