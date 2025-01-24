package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record ReorderRuleCommand(long id, int sort) implements ApplicationEvent {

    public static void reorderRuleUpdated(long id, int sort) {
        new ReorderRuleCommand(id, sort)
                .publish();
    }
}
