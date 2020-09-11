package com.jongsoft.finance.bpmn.delegate.rule;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.RuleConfigJson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ProcessTransactionRuleDelegate implements JavaDelegate {

    private final CurrentUserProvider currentUserProvider;

    public ProcessTransactionRuleDelegate(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing transaction rule configuration {}",
                execution.getCurrentActivityName(),
                execution.getActivityInstanceId());

        var ruleJson = (RuleConfigJson.RuleJson) execution.getVariableLocal("ruleConfiguration");

        var userAccount = currentUserProvider.currentUser();
        var transactionRule = userAccount.createRule(ruleJson.getName(), ruleJson.isRestrictive());

        ruleJson.getConditions()
                .forEach(c -> transactionRule.registerCondition(
                        c.getField(),
                        c.getOperation(),
                        c.getValue()));

        transactionRule.change(
                ruleJson.getName(),
                ruleJson.getDescription(),
                ruleJson.isRestrictive(),
                ruleJson.isActive());

        transactionRule.changeOrder(ruleJson.getSort());

        execution.setVariable("transactionRule", transactionRule);
    }

}
