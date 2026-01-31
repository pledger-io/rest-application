package com.jongsoft.finance.classification.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record RenameCategoryCommand(long id, String name, String description)
        implements ApplicationEvent {

    public static void categoryRenamed(long id, String name, String description) {
        new RenameCategoryCommand(id, name, description).publish();
    }
}
