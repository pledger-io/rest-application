package com.jongsoft.finance.suggestion.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record RuleRemovedCommand(long ruleId) implements ApplicationEvent {

    public static void ruleRemoved(long ruleId) {
        new RuleRemovedCommand(ruleId).publish();
    }
}
