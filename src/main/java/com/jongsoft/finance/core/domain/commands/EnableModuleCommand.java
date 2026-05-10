package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.util.UUID;

public record EnableModuleCommand(UUID id) implements ApplicationEvent {
    public static void moduleEnabled(UUID id) {
        new EnableModuleCommand(id).publish();
    }
}
