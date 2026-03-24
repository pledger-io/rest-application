package com.jongsoft.finance.project.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record DeleteTimeEntryCommand(long id) implements ApplicationEvent {

    public static void timeEntryDeleted(long id) {
        new DeleteTimeEntryCommand(id).publish();
    }
}
