package com.jongsoft.finance.contract.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record TerminateContractCommand(long id) implements ApplicationEvent {

    /**
     * Terminates a contract identified by the given ID by creating and publishing a
     * TerminateContractCommand event.
     *
     * @param id the ID of the contract to be terminated
     */
    public static void contractTerminated(long id) {
        new TerminateContractCommand(id).publish();
    }
}
