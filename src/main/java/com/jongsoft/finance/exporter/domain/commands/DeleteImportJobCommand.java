package com.jongsoft.finance.exporter.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record DeleteImportJobCommand(long id) implements ApplicationEvent {

    public static void importJobDeleted(long id) {
        new DeleteImportJobCommand(id).publish();
    }
}
