package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record RenameRuleGroupCommand(long id, String name) implements ApplicationEvent {

    public static void ruleGroupRenamed(long id, String name) {
        new RenameRuleGroupCommand(id, name)
                .publish();
    }
}
