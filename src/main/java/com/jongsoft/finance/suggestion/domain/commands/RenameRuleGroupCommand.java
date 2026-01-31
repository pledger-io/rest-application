package com.jongsoft.finance.suggestion.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record RenameRuleGroupCommand(long id, String name) implements ApplicationEvent {

    public static void ruleGroupRenamed(long id, String name) {
        new RenameRuleGroupCommand(id, name).publish();
    }
}
