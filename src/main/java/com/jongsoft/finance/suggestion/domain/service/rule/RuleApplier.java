package com.jongsoft.finance.suggestion.domain.service.rule;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.banking.types.TransactionLinkType;
import com.jongsoft.finance.budget.adapter.api.ExpenseProvider;
import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.contract.adapter.api.ContractProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.suggestion.domain.commands.ApplyTransactionRulesCommand;
import com.jongsoft.finance.suggestion.types.RuleColumn;
import com.jongsoft.lang.collection.Sequence;

import io.micrometer.core.annotation.Timed;
import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

@Singleton
class RuleApplier {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(RuleApplier.class);

    private final RuleSuggestionEngine ruleSuggestionEngine;
    private final TransactionProvider transactionProvider;
    private final CategoryProvider categoryProvider;
    private final ContractProvider contractProvider;
    private final ExpenseProvider expenseProvider;
    private final FilterProvider<ExpenseProvider.FilterCommand> filterProvider;

    RuleApplier(
            RuleSuggestionEngine ruleSuggestionEngine,
            TransactionProvider transactionProvider,
            CategoryProvider categoryProvider,
            ContractProvider contractProvider,
            ExpenseProvider expenseProvider,
            FilterProvider<ExpenseProvider.FilterCommand> filterProvider) {
        this.ruleSuggestionEngine = ruleSuggestionEngine;
        this.transactionProvider = transactionProvider;
        this.categoryProvider = categoryProvider;
        this.contractProvider = contractProvider;
        this.expenseProvider = expenseProvider;
        this.filterProvider = filterProvider;
    }

    @EventListener
    @Timed(
            value = "learning.rule-based",
            extraTags = {"action", "apply-rules"})
    void applyRulesToTransaction(ApplyTransactionRulesCommand command) {
        log.debug("Applying rules to transaction {}", command.transactionId());
        RuleDataSet dataSet = new RuleDataSet();

        Transaction transaction =
                transactionProvider.lookup(command.transactionId()).get();
        dataSet.put(RuleColumn.TO_ACCOUNT, transaction.computeTo().getName());
        dataSet.put(RuleColumn.SOURCE_ACCOUNT, transaction.computeFrom().getName());
        dataSet.put(RuleColumn.AMOUNT, transaction.computeAmount(transaction.computeTo()));
        dataSet.put(RuleColumn.DESCRIPTION, transaction.getDescription());

        RuleDataSet suggestions = ruleSuggestionEngine.run(dataSet);
        for (Map.Entry<RuleColumn, ?> entry : suggestions.entrySet()) {
            switch (entry.getKey()) {
                case CATEGORY ->
                    categoryProvider
                            .lookup(entry.getValue().toString())
                            .ifPresent(category -> transaction.link(
                                    TransactionLinkType.CATEGORY, category.getId()));
                case TO_ACCOUNT, CHANGE_TRANSFER_TO ->
                    transaction.changeAccount(false, (Account) entry.getValue());
                case SOURCE_ACCOUNT, CHANGE_TRANSFER_FROM ->
                    transaction.changeAccount(true, (Account) entry.getValue());
                case CONTRACT ->
                    contractProvider
                            .lookup(entry.getValue().toString())
                            .ifPresent(contract -> transaction.link(
                                    TransactionLinkType.CONTRACT, contract.getId()));
                case BUDGET ->
                    Optional.of(expenseProvider.lookup(filterProvider
                                    .create()
                                    .name(entry.getValue().toString(), true)))
                            .map(ResultPage::content)
                            .map(Sequence::head)
                            .ifPresent(value ->
                                    transaction.link(TransactionLinkType.EXPENSE, value.getId()));
                default ->
                    throw new IllegalArgumentException(
                            "Unsupported rule column provided " + entry.getKey());
            }
        }
    }
}
