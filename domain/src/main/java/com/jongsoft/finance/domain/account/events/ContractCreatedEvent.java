package com.jongsoft.finance.domain.account.events;

import java.time.LocalDate;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.account.Account;

import lombok.Getter;

@Getter
public class ContractCreatedEvent implements ApplicationEvent {

    private final Account company;
    private final String name;
    private final String description;
    private final LocalDate start;
    private final LocalDate end;

    /**
     * Create a new ContractCreated event
     * @param source        the object on which the event initially occurred (never {@code null})
     * @param company       the company
     * @param name          the name of the contract
     * @param description   the description
     * @param start         the start date
     * @param end           the end date
     */
    public ContractCreatedEvent(Object source, Account company, String name, String description, LocalDate start, LocalDate end) {
        this.company = company;
        this.name = name;
        this.description = description;
        this.start = start;
        this.end = end;
    }

}
