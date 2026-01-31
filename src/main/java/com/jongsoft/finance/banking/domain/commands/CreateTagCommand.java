package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CreateTagCommand(String tag) implements ApplicationEvent {

    public static void tagCreated(String tag) {
        new CreateTagCommand(tag).publish();
    }
}
