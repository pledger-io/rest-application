package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record DeleteTaxBracketCommand(long id) implements ApplicationEvent {
    public static void taxBracketDeleted(long id) {
        new DeleteTaxBracketCommand(id).publish();
    }
}
