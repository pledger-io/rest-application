package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.inject.Singleton;
import java.util.Objects;

@Slf4j
@Singleton
public class TransactionRuleLookupDelegate implements JavaDelegate {

    private final TransactionRuleProvider transactionRuleProvider;

    public TransactionRuleLookupDelegate(TransactionRuleProvider transactionRuleProvider) {
        this.transactionRuleProvider = transactionRuleProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Lookup transaction rule by name {}",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"));

        var ruleName = (String) execution.getVariableLocal("name");

        var existing = transactionRuleProvider.lookup()
                .count(rule -> Objects.equals(rule.getName(), ruleName));

        execution.setVariableLocal("exists", existing != 0);
    }

}