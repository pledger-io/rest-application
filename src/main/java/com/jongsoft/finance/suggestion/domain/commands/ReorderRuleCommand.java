package com.jongsoft.finance.suggestion.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record ReorderRuleCommand(long id, int sort) implements ApplicationEvent {

    public static void reorderRuleUpdated(long id, int sort) {
        new ReorderRuleCommand(id, sort).publish();
    }
}
