package com.jongsoft.finance.classification.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CreateCategoryCommand(String name, String description) implements ApplicationEvent {

    public static void categoryCreated(String name, String description) {
        new CreateCategoryCommand(name, description).publish();
    }
}
