package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class ContractTerminatedEvent implements ApplicationEvent {

    private final Long id;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param id     the id of the contract
     */
    public ContractTerminatedEvent(Object source, Long id) {
        this.id = id;
    }

}
