package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record DeleteTagCommand(String tag) implements ApplicationEvent {

    public static void tagDeleted(String tag) {
        new DeleteTagCommand(tag).publish();
    }
}
