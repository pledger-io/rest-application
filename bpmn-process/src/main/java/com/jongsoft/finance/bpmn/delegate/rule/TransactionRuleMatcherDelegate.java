package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class TransactionRuleMatcherDelegate implements JavaDelegate {

    private final RuleEngine ruleEngine;
    private final TransactionProvider transactionProvider;
    private final TransactionRuleProvider ruleProvider;

    public TransactionRuleMatcherDelegate(
            RuleEngine ruleEngine,
            TransactionProvider transactionProvider,
            TransactionRuleProvider ruleProvider) {
        this.ruleEngine = ruleEngine;
        this.transactionProvider = transactionProvider;
        this.ruleProvider = ruleProvider;
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

        for (RuleColumn column : outputSet.keySet()) {
            switch (column) {
                case CATEGORY -> transaction.linkToCategory((String) outputSet.get(column));
                case TO_ACCOUNT, CHANGE_TRANSFER_TO -> transaction.changeAccount(false, (Account) outputSet.get(column));
                case SOURCE_ACCOUNT, CHANGE_TRANSFER_FROM -> transaction.changeAccount(true, (Account) outputSet.get(column));
                case CONTRACT -> transaction.linkToContract((String) outputSet.get(column));
                case BUDGET -> transaction.linkToBudget((String) outputSet.get(column));
                default -> throw new IllegalArgumentException("Unsupported rule column provided " + column);
            }
        }
    }
}
