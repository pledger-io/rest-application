package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record RegisterSynonymCommand(long accountId, String synonym) implements ApplicationEvent {

    public static void synonymRegistered(long accountId, String synonym) {
        new RegisterSynonymCommand(accountId, synonym).publish();
    }
}
