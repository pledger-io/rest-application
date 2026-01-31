package com.jongsoft.finance.contract.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record ContractWarningSend(long contractId) implements ApplicationEvent {
    public static void warningSent(long contractId) {
        new ContractWarningSend(contractId).publish();
    }
}
