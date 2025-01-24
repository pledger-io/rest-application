package com.jongsoft.finance.messaging.commands.importer;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateImportJobCommand(long configId, String slug, String fileCode) implements ApplicationEvent {

    public static void importJobCreated(long configId, String slug, String fileCode) {
        new CreateImportJobCommand(configId, slug, fileCode)
                .publish();
    }
}
