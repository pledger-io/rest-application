package com.jongsoft.finance.messaging.commands.importer;

import com.jongsoft.finance.core.ApplicationEvent;

public record CompleteImportJobCommand(long id) implements ApplicationEvent {
}
