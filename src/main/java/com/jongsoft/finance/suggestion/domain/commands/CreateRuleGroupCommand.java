package com.jongsoft.finance.suggestion.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CreateRuleGroupCommand(String name) implements ApplicationEvent {

    public static void ruleGroupCreated(String name) {
        new CreateRuleGroupCommand(name).publish();
    }
}
