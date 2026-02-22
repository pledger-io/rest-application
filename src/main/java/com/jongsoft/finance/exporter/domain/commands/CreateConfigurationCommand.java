package com.jongsoft.finance.exporter.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CreateConfigurationCommand(String type, String name, String fileCode)
        implements ApplicationEvent {

    public static void configurationCreated(String type, String name, String fileCode) {
        new CreateConfigurationCommand(type, name, fileCode).publish();
    }
}
