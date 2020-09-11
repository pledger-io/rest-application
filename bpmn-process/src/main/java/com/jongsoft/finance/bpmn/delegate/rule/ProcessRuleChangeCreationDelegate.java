package com.jongsoft.finance.bpmn.delegate.rule;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.transaction.TransactionRule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ProcessRuleChangeCreationDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        TransactionRule transactionRule = (TransactionRule) execution.getVariableLocal("entity");
        String id  = (String) execution.getVariableLocal("value");
        RuleColumn field = (RuleColumn) execution.getVariableLocal("field");

        log.debug("{}: Processing transaction rule {} change addition {}|{}", execution.getCurrentActivityName(),
                transactionRule.getName(), field, id);

        transactionRule.registerChange(field, id);
    }

}
