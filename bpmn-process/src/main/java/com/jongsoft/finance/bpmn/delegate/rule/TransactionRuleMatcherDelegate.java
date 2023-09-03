package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;

import java.util.Map;

@Slf4j
@Singleton
public class TransactionRuleMatcherDelegate implements JavaDelegate {

    private final RuleEngine ruleEngine;
    private final TransactionProvider transactionProvider;

    TransactionRuleMatcherDelegate(RuleEngine ruleEngine, TransactionProvider transactionProvider) {
        this.ruleEngine = ruleEngine;
        this.transactionProvider = transactionProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var id = execution.<LongValue>getVariableLocalTyped("transactionId").getValue();

        var transaction = transactionProvider.lookup(id)
                .getOrThrow(() -> new IllegalStateException("Cannot locate transaction with id " + id));

        log.debug("{}: Processing transaction rules on transaction {}",
                execution.getCurrentActivityName(),
                transaction.getId());

        var inputSet = new RuleDataSet();
        inputSet.put(RuleColumn.TO_ACCOUNT, transaction.computeTo().getName());
        inputSet.put(RuleColumn.SOURCE_ACCOUNT, transaction.computeFrom().getName());
        inputSet.put(RuleColumn.AMOUNT, transaction.computeAmount(transaction.computeTo()));
        inputSet.put(RuleColumn.DESCRIPTION, transaction.getDescription());

        var outputSet = ruleEngine.run(inputSet);

        for (Map.Entry<RuleColumn, ?> entry : outputSet.entrySet()) {
            switch (entry.getKey()) {
                case CATEGORY -> transaction.linkToCategory((String) entry.getValue());
                case TO_ACCOUNT, CHANGE_TRANSFER_TO -> transaction.changeAccount(false, (Account) entry.getValue());
                case SOURCE_ACCOUNT, CHANGE_TRANSFER_FROM -> transaction.changeAccount(true, (Account) entry.getValue());
                case CONTRACT -> transaction.linkToContract((String) entry.getValue());
                case BUDGET -> transaction.linkToBudget((String) entry.getValue());
                default -> throw new IllegalArgumentException("Unsupported rule column provided " + entry.getKey());
            }
        }
    }
}
