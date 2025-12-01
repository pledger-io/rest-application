package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record RuleRemovedCommand(long ruleId) implements ApplicationEvent {

    public static void ruleRemoved(long ruleId) {
        new RuleRemovedCommand(ruleId).publish();
    }
}
