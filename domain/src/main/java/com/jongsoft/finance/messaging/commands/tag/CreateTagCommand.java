package com.jongsoft.finance.messaging.commands.tag;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateTagCommand(String tag) implements ApplicationEvent {

    public static void tagCreated(String tag) {
        new CreateTagCommand(tag)
                .publish();
    }
}
