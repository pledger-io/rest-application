package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;

import java.time.LocalDate;

public record ChangeTransactionDatesCommand(long id, LocalDate date, LocalDate bookingDate, LocalDate interestDate)
        implements ApplicationEvent {
}
