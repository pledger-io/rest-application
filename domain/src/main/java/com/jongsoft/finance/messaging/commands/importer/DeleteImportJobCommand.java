package com.jongsoft.finance.messaging.commands.importer;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record DeleteImportJobCommand(long id) implements ApplicationEvent {

    public static void importJobDeleted(long id) {
        new DeleteImportJobCommand(id).publish();
    }
}
