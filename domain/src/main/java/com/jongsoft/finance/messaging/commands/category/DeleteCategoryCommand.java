package com.jongsoft.finance.messaging.commands.category;

import com.jongsoft.finance.core.ApplicationEvent;

public record DeleteCategoryCommand(long id) implements ApplicationEvent {
}
