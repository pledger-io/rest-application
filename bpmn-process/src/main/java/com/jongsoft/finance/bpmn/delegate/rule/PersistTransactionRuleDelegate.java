package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
@Singleton
public class PersistTransactionRuleDelegate implements JavaDelegate, JavaBean {

    private final TransactionRuleProvider ruleProvider;

    PersistTransactionRuleDelegate(TransactionRuleProvider ruleProvider) {
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
