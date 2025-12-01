package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateRuleGroupCommand(String name) implements ApplicationEvent {

    public static void ruleGroupCreated(String name) {
        new CreateRuleGroupCommand(name).publish();
    }
}
