package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.rule.CreateRuleGroupCommand;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
@Singleton
public class RuleGroupLookupDelegate implements JavaDelegate, JavaBean {

    private final TransactionRuleGroupProvider ruleGroupProvider;

    RuleGroupLookupDelegate(TransactionRuleGroupProvider ruleGroupProvider) {
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
        EventBus.getBus().send(new CreateRuleGroupCommand(name));

        return ruleGroupProvider.lookup(name)
                .getOrThrow(() -> new IllegalStateException("Failed to create rule group with name " + name));
    }

}
