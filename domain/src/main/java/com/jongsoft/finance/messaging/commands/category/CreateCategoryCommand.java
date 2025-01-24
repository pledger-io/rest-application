package com.jongsoft.finance.messaging.commands.category;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateCategoryCommand(String name, String description) implements ApplicationEvent {

    public static void categoryCreated(String name, String description) {
        new CreateCategoryCommand(name, description)
                .publish();
    }
}
