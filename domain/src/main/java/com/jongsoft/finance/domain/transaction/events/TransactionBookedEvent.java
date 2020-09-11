package com.jongsoft.finance.domain.transaction.events;

import java.time.LocalDate;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransactionBookedEvent implements ApplicationEvent {

    private final Long transactionId;
    private final LocalDate date;
    private final LocalDate interestDate;
    private final LocalDate bookDate;

    public TransactionBookedEvent(Object source, Long transactionId, LocalDate date, LocalDate interestDate, LocalDate bookDate) {
        this.transactionId = transactionId;
        this.date = date;
        this.interestDate = interestDate;
        this.bookDate = bookDate;
    }

}
