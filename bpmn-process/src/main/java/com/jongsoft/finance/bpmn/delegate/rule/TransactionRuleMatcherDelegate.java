package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.Classifier;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import com.jongsoft.lang.Value;
import com.jongsoft.lang.collection.Sequence;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This delegate is responsible for matching a transaction against the rules engine.
 *
 * <p>The transaction is retrieved from the {@link TransactionProvider} and the rules engine is
 * invoked with the transaction data. <br>
 * The output of the rules engine is then applied to the transaction. <br>
 * The transaction is then persisted back to the {@link TransactionProvider}.
 */
@Slf4j
@Singleton
public class TransactionRuleMatcherDelegate implements JavaDelegate, JavaBean {

    private final RuleEngine ruleEngine;
    private final TransactionProvider transactionProvider;
    private final List<DataProvider<Classifier>> dataProviders;
    private final FilterFactory filterFactory;

    TransactionRuleMatcherDelegate(
            RuleEngine ruleEngine,
            TransactionProvider transactionProvider,
            List<DataProvider<Classifier>> dataProviders,
            FilterFactory filterFactory) {
        this.ruleEngine = ruleEngine;
        this.transactionProvider = transactionProvider;
        this.dataProviders = dataProviders;
        this.filterFactory = filterFactory;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var id = execution.<LongValue>getVariableLocalTyped("transactionId").getValue();

        var transaction = transactionProvider
                .lookup(id)
                .getOrThrow(
                        () -> new IllegalStateException("Cannot locate transaction with id " + id));

        log.debug(
                "{}: Processing transaction rules on transaction {}",
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
                case CATEGORY ->
                    this.<CategoryProvider>lookupDataProvider("CATEGORY")
                            .map(provider -> provider.lookup((String) entry.getValue()))
                            .filter(com.jongsoft.lang.control.Optional::isPresent)
                            .map(Value::get)
                            .ifPresent(value -> transaction.link(
                                    LinkTransactionCommand.LinkType.CATEGORY, value.getId()));
                case TO_ACCOUNT, CHANGE_TRANSFER_TO ->
                    transaction.changeAccount(false, (Account) entry.getValue());
                case SOURCE_ACCOUNT, CHANGE_TRANSFER_FROM ->
                    transaction.changeAccount(true, (Account) entry.getValue());
                case CONTRACT ->
                    this.<ContractProvider>lookupDataProvider("CONTRACT")
                            .map(provider -> provider.lookup((String) entry.getValue()))
                            .filter(com.jongsoft.lang.control.Optional::isPresent)
                            .map(Value::get)
                            .ifPresent(value -> transaction.link(
                                    LinkTransactionCommand.LinkType.CONTRACT, value.getId()));
                case BUDGET ->
                    this.<ExpenseProvider>lookupDataProvider("EXPENSE")
                            .map(provider -> provider.lookup(
                                    filterFactory.expense().name((String) entry.getValue(), true)))
                            .map(ResultPage::content)
                            .map(Sequence::head)
                            .ifPresent(value -> transaction.link(
                                    LinkTransactionCommand.LinkType.EXPENSE, value.getId()));
                default ->
                    throw new IllegalArgumentException(
                            "Unsupported rule column provided " + entry.getKey());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends DataProvider<? extends Classifier>> Optional<T> lookupDataProvider(
            String type) {
        return (Optional<T>) dataProviders.stream()
                .filter(provider -> Objects.equals(provider.typeOf(), type))
                .findFirst();
    }
}
