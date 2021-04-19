package com.jongsoft.finance.messaging.commands.importer;

import com.jongsoft.finance.core.ApplicationEvent;

public record DeleteImportJobCommand(long id) implements ApplicationEvent {
}
