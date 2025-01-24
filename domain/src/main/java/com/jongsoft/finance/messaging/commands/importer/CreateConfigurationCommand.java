package com.jongsoft.finance.messaging.commands.importer;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateConfigurationCommand(String type, String name, String fileCode) implements ApplicationEvent {

    public static void configurationCreated(String type, String name, String fileCode) {
        new CreateConfigurationCommand(type, name, fileCode)
                .publish();
    }
}
