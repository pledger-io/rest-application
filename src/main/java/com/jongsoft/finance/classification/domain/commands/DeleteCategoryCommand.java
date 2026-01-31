package com.jongsoft.finance.classification.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record DeleteCategoryCommand(long id) implements ApplicationEvent {

    public static void categoryDeleted(long id) {
        new DeleteCategoryCommand(id).publish();
    }
}
