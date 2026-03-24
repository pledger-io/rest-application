package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record DeleteInvoiceTemplateCommand(long id) implements ApplicationEvent {
    public static void templateDeleted(long id) {
        new DeleteInvoiceTemplateCommand(id).publish();
    }
}
