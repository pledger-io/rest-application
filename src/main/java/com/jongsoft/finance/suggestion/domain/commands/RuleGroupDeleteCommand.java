package com.jongsoft.finance.suggestion.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record RuleGroupDeleteCommand(long id) implements ApplicationEvent {

    public static void ruleGroupDeleted(long id) {
        new RuleGroupDeleteCommand(id).publish();
    }
}
