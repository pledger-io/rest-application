package com.jongsoft.finance.domain.account.events;

import java.time.LocalDate;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class ContractChangedEvent implements ApplicationEvent {

    private final Long id;
    private final String name;
    private final String description;
    private final LocalDate start;
    private final LocalDate end;

    /**
     * Create a new ContractCreated event
     *  @param source    the object on which the event initially occurred (never {@code null})
     * @param id        the identifier
     * @param name      the name of the contract
     * @param description
     * @param start     the start date
     * @param end       the end date
     */
    public ContractChangedEvent(Object source, Long id, String name, String description, LocalDate start, LocalDate end) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.start = start;
        this.end = end;
    }

}
