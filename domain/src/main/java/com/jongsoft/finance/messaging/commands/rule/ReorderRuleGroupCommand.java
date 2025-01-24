package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record ReorderRuleGroupCommand(long id, int sort) implements ApplicationEvent {

    public static void reorderRuleGroupUpdated(long id, int sort) {
        new ReorderRuleGroupCommand(id, sort)
                .publish();
    }
}
