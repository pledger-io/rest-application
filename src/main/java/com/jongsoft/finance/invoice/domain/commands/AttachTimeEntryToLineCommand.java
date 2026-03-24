package com.jongsoft.finance.invoice.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record AttachTimeEntryToLineCommand(long invoiceLineId, long timeEntryId)
        implements ApplicationEvent {

    public static void timeEntryAttached(long invoiceLineId, long timeEntryId) {
        new AttachTimeEntryToLineCommand(invoiceLineId, timeEntryId).publish();
    }
}
