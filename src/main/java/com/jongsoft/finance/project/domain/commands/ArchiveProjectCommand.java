package com.jongsoft.finance.project.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record ArchiveProjectCommand(long id) implements ApplicationEvent {

    public static void projectArchived(long id) {
        new ArchiveProjectCommand(id).publish();
    }
}
