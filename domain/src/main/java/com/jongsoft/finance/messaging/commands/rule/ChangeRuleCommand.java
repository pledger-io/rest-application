package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.messaging.ApplicationEvent;

public record ChangeRuleCommand(long id, RuleColumn column, String change)
    implements ApplicationEvent {

  public static void changeRuleUpdated(long id, RuleColumn column, String change) {
    new ChangeRuleCommand(id, column, change).publish();
  }
}
