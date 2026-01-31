package com.jongsoft.finance.suggestion.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record ReorderRuleGroupCommand(long id, int sort) implements ApplicationEvent {

    public static void reorderRuleGroupUpdated(long id, int sort) {
        new ReorderRuleGroupCommand(id, sort).publish();
    }
}
