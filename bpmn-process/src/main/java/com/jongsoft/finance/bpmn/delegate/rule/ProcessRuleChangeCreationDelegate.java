package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.transaction.TransactionRule;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
@Singleton
public class ProcessRuleChangeCreationDelegate implements JavaDelegate, JavaBean {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        TransactionRule transactionRule = (TransactionRule) execution.getVariableLocal("entity");
        String id = (String) execution.getVariableLocal("value");
        RuleColumn field = (RuleColumn) execution.getVariableLocal("field");

        log.debug(
                "{}: Processing transaction rule {} change addition {}|{}",
                execution.getCurrentActivityName(),
                transactionRule.getName(),
                field,
                id);

        transactionRule.registerChange(field, id);
    }
}
