package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PersistTransactionRuleDelegate implements JavaDelegate {

    private final TransactionRuleProvider ruleProvider;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        TransactionRule transactionRule = (TransactionRule) execution.getVariableLocal("transactionRule");

        log.debug("{}: Processing transaction rule save {}", execution.getCurrentActivityName(),
                transactionRule.getName());

        ruleProvider.save(transactionRule);
    }

}
