package com.jongsoft.finance.exporter.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CompleteImportJobCommand(long id) implements ApplicationEvent {

    public static void importJobCompleted(long id) {
        new CompleteImportJobCommand(id).publish();
    }
}
