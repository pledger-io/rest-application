package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.ApplicationEvent;

public record ChangeMultiFactorCommand(UserIdentifier username, boolean enabled)
        implements ApplicationEvent {

    public static void multiFactorChanged(UserIdentifier username, boolean enabled) {
        new ChangeMultiFactorCommand(username, enabled).publish();
    }
}
