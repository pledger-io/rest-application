package com.jongsoft.finance.messaging.commands.category;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record RenameCategoryCommand(long id, String name, String description)
        implements ApplicationEvent {

    public static void categoryRenamed(long id, String name, String description) {
        new RenameCategoryCommand(id, name, description).publish();
    }
}
