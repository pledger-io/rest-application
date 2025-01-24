package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record ChangeMultiFactorCommand(String username, boolean enabled) implements ApplicationEvent {

    public static void multiFactorChanged(String username, boolean enabled) {
        new ChangeMultiFactorCommand(username, enabled)
                .publish();
    }
}
