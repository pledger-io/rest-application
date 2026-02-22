package com.jongsoft.finance.suggestion.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.suggestion.types.RuleColumn;

public record ChangeRuleCommand(long id, RuleColumn column, String change)
        implements ApplicationEvent {

    public static void changeRuleUpdated(long id, RuleColumn column, String change) {
        new ChangeRuleCommand(id, column, change).publish();
    }
}
