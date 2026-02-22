package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record TerminateAccountCommand(long id) implements ApplicationEvent {

    /**
     * Terminates the account identified by the specified ID.
     *
     * @param id the identifier of the account to be terminated
     */
    public static void accountTerminated(long id) {
        new TerminateAccountCommand(id).publish();
    }
}
