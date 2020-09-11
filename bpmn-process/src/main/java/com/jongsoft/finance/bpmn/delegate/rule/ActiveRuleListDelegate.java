package com.jongsoft.finance.bpmn.delegate.rule;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import com.jongsoft.lang.API;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ActiveRuleListDelegate implements JavaDelegate {

    private final TransactionRuleProvider ruleProvider;

    @Inject
    public ActiveRuleListDelegate(TransactionRuleProvider ruleProvider) {
        this.ruleProvider = ruleProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Loading the active transaction rule list", execution.getCurrentActivityName());

        var update = API.Option(execution.getVariableLocal("update"))
                .map(Object::toString)
                .map(Boolean::valueOf)
                .getOrSupply(() -> false);

        var ruleIds = ruleProvider.lookup()
                .filter(rule -> rule.isActive() || !update)
                .map(TransactionRule::getId)
                .toJava();

        execution.setVariableLocal("ids", ruleIds);
    }
}
