package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.messaging.ApplicationEvent;

import java.time.LocalDate;

public record ChangeTransactionDatesCommand(
        long id, LocalDate date, LocalDate bookingDate, LocalDate interestDate)
        implements ApplicationEvent {

    public static void transactionDatesChanged(
            long id, LocalDate date, LocalDate bookingDate, LocalDate interestDate) {
        new ChangeTransactionDatesCommand(id, date, bookingDate, interestDate).publish();
    }
}
