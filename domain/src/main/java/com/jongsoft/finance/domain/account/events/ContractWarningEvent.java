package com.jongsoft.finance.domain.account.events;

import java.time.LocalDate;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class ContractWarningEvent implements ApplicationEvent {

    private final Long contractId;
    private final LocalDate endDate;

    /**
     * Create a new ContractWarningEvent.
     *  @param source        the object on which the event initially occurred (never {@code null})
     * @param contractId    the contract to be warned about
     * @param endDate
     */
    public ContractWarningEvent(Object source, Long contractId, LocalDate endDate) {
        this.contractId = contractId;
        this.endDate = endDate;
    }

}
