package com.jongsoft.finance.messaging.commands.category;

import com.jongsoft.finance.core.ApplicationEvent;

public record CreateCategoryCommand(String name, String description) implements ApplicationEvent {
}
