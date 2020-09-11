package com.jongsoft.finance.bpmn.delegate.rule;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class PersistTransactionRuleDelegate implements JavaDelegate {

    private final TransactionRuleProvider ruleProvider;

    @Inject
    public PersistTransactionRuleDelegate(TransactionRuleProvider ruleProvider) {
        this.ruleProvider = ruleProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        TransactionRule transactionRule = (TransactionRule) execution.getVariableLocal("transactionRule");

        log.debug("{}: Processing transaction rule save {}", execution.getCurrentActivityName(),
                transactionRule.getName());

        ruleProvider.save(transactionRule);
    }

}
