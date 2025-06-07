package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateRuleGroupCommand(String name) implements ApplicationEvent {}
