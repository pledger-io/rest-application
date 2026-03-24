package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;
import java.util.List;

public record AddInvoiceLineCommand(
        long invoiceId,
        String description,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        long taxBracketId,
        List<Long> timeEntryIds)
        implements ApplicationEvent {

    public static void lineAdded(
            long invoiceId,
            String description,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            long taxBracketId) {
        new AddInvoiceLineCommand(
                        invoiceId, description, quantity, unit, unitPrice, taxBracketId, List.of())
                .publish();
    }

    public static void lineAddedWithTimeEntries(
            long invoiceId,
            String description,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            long taxBracketId,
            List<Long> timeEntryIds) {
        new AddInvoiceLineCommand(
                        invoiceId,
                        description,
                        quantity,
                        unit,
                        unitPrice,
                        taxBracketId,
                        timeEntryIds)
                .publish();
    }
}
