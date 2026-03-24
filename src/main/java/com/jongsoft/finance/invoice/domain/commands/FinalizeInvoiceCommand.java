package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record FinalizeInvoiceCommand(long id, String pdfToken) implements ApplicationEvent {

    public static void invoiceFinalized(long id, String pdfToken) {
        new FinalizeInvoiceCommand(id, pdfToken).publish();
    }
}
