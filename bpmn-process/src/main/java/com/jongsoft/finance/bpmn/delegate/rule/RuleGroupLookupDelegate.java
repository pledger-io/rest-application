package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.rule.CreateRuleGroupCommand;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RuleGroupLookupDelegate implements JavaDelegate {

    private final TransactionRuleGroupProvider ruleGroupProvider;

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
