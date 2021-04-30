package com.jongsoft.finance.messaging.commands.importer;

import com.jongsoft.finance.core.ApplicationEvent;

public record CreateImportJobCommand(long configId, String slug, String fileCode) implements ApplicationEvent {
}
