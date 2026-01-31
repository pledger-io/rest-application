package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.core.value.UserIdentifier;

public record ChangeMultiFactorCommand(UserIdentifier username, boolean enabled)
        implements ApplicationEvent {

    public static void multiFactorChanged(UserIdentifier username, boolean enabled) {
        new ChangeMultiFactorCommand(username, enabled).publish();
    }
}
