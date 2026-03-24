package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CreateInvoiceTemplateCommand(
        String name, String headerContent, String footerContent, String logoToken)
        implements ApplicationEvent {

    public static void templateCreated(
            String name, String headerContent, String footerContent, String logoToken) {
        new CreateInvoiceTemplateCommand(name, headerContent, footerContent, logoToken).publish();
    }
}
