package com.jongsoft.finance.messaging.commands.category;

import com.jongsoft.finance.core.ApplicationEvent;

public record RenameCategoryCommand(long id, String name, String description)
        implements ApplicationEvent {
}
