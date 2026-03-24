package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.LocalDate;

public record UpdateInvoiceCommand(
        long id, String invoiceNumber, LocalDate invoiceDate, LocalDate dueDate)
        implements ApplicationEvent {

    public static void invoiceUpdated(
            long id, String invoiceNumber, LocalDate invoiceDate, LocalDate dueDate) {
        new UpdateInvoiceCommand(id, invoiceNumber, invoiceDate, dueDate).publish();
    }
}
