package com.jongsoft.finance.messaging.commands.importer;

import com.jongsoft.finance.core.ApplicationEvent;

public record CreateConfigurationCommand(String type, String name, String fileCode) implements ApplicationEvent {
}
