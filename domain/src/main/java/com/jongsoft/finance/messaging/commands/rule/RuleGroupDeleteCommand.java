package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record RuleGroupDeleteCommand(long id) implements ApplicationEvent {

  public static void ruleGroupDeleted(long id) {
    new RuleGroupDeleteCommand(id).publish();
  }
}
