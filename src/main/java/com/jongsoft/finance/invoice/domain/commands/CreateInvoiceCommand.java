package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.LocalDate;

public record CreateInvoiceCommand(
        long clientId,
        String invoiceNumber,
        LocalDate invoiceDate,
        LocalDate dueDate,
        long templateId)
        implements ApplicationEvent {

    public static void invoiceCreated(
            long clientId,
            String invoiceNumber,
            LocalDate invoiceDate,
            LocalDate dueDate,
            long templateId) {
        new CreateInvoiceCommand(clientId, invoiceNumber, invoiceDate, dueDate, templateId)
                .publish();
    }
}
