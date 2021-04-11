package com.jongsoft.finance.bpmn.delegate.rule;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupCreatedEvent;
import com.jongsoft.finance.messaging.EventBus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RuleGroupLookupDelegate implements JavaDelegate {

    private final TransactionRuleGroupProvider ruleGroupProvider;

    public RuleGroupLookupDelegate(TransactionRuleGroupProvider ruleGroupProvider) {
        this.ruleGroupProvider = ruleGroupProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing rule group lookup {}",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"));

        String name = (String) execution.getVariableLocal("name");

        var group = ruleGroupProvider.lookup(name)
                .getOrSupply(() -> createRuleGroup(name));

        execution.setVariable("group", group);
    }

    private TransactionRuleGroup createRuleGroup(String name) {
        EventBus.getBus().send(new TransactionRuleGroupCreatedEvent(
                this,
                name));

        return ruleGroupProvider.lookup(name)
                .getOrThrow(() -> new IllegalStateException("Failed to create rule group with name " + name));
    }

}
