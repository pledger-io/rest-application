package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record UpdateInvoiceTemplateCommand(
        long id, String name, String headerContent, String footerContent, String logoToken)
        implements ApplicationEvent {

    public static void templateUpdated(
            long id, String name, String headerContent, String footerContent, String logoToken) {
        new UpdateInvoiceTemplateCommand(id, name, headerContent, footerContent, logoToken)
                .publish();
    }
}
