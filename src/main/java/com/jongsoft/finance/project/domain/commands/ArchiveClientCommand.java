package com.jongsoft.finance.project.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record ArchiveClientCommand(long id) implements ApplicationEvent {

    public static void clientArchived(long id) {
        new ArchiveClientCommand(id).publish();
    }
}
