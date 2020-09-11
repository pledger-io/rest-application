package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class ContractUploadEvent implements ApplicationEvent {

    private final Long id;
    private final String storageToken;

    /**
     * Create a new contract uploaded event.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param id
     * @param storageToken
     */
    public ContractUploadEvent(Object source, Long id, String storageToken) {
        this.id = id;
        this.storageToken = storageToken;
    }

}
