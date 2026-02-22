package com.jongsoft.finance.exporter.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CreateImportJobCommand(long configId, String slug, String fileCode)
        implements ApplicationEvent {

    public static void importJobCreated(long configId, String slug, String fileCode) {
        new CreateImportJobCommand(configId, slug, fileCode).publish();
    }
}
