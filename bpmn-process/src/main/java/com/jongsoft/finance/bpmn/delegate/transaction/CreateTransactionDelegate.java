package com.jongsoft.finance.bpmn.delegate.transaction;

import static com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand.LinkType.IMPORT;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.Classifier;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.importer.api.TransactionDTO;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.*;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.Value;
import com.jongsoft.lang.collection.Sequence;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Delegate for creating a transaction in the system.
 *
 * <p>This delegate is responsible for creating a transaction in the system. It is used in the BPMN
 * process to create a transaction from the parsed transaction data.
 *
 * <p>The delegate expects the following variables to be present in the execution:
 *
 * <ul>
 *   <li>transaction: The parsed transaction data from the import job
 *   <li>accountId: The ID of the account to create the transaction in
 *   <li>targetAccount: The ID of the account to create the transaction for
 *   <li>importJobSlug: The slug of the import job that the transaction is part of
 * </ul>
 *
 * The delegate will create the transaction in the target account and set the {@code transactionId}
 * in the execution.
 */
@Slf4j
@Singleton
public class CreateTransactionDelegate implements JavaDelegate, JavaBean {

    private final AccountProvider accountProvider;
    private final TransactionCreationHandler creationHandler;

    private final List<DataProvider<Classifier>> dataProviders;
    private final FilterFactory filterFactory;

    CreateTransactionDelegate(
            AccountProvider accountProvider,
            TransactionCreationHandler creationHandler,
            List<DataProvider<Classifier>> dataProviders,
            FilterFactory filterFactory) {
        this.accountProvider = accountProvider;
        this.creationHandler = creationHandler;
        this.dataProviders = dataProviders;
        this.filterFactory = filterFactory;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var batchImportSlug = (String) execution.getVariable("importJobSlug");
        var parsedTransaction = (TransactionDTO) execution.getVariableLocal("transaction");
        var toAccount = lookupAccount(execution, "accountId");
        var targetAccount = lookupAccount(execution, "targetAccount");

        log.debug(
                "{}: Creating transaction into {} from {} with amount {}",
                execution.getCurrentActivityName(),
                targetAccount.getName(),
                toAccount.getName(),
                parsedTransaction.amount());

        var type =
                switch (parsedTransaction.type()) {
                    case DEBIT -> Transaction.Type.DEBIT;
                    case CREDIT -> Transaction.Type.CREDIT;
                    case TRANSFER -> Transaction.Type.TRANSFER;
                };

        Transaction transaction = targetAccount.createTransaction(
                toAccount, parsedTransaction.amount(), type, t -> t.currency(
                                targetAccount.getCurrency())
                        .date(parsedTransaction.transactionDate())
                        .bookDate(parsedTransaction.bookDate())
                        .interestDate(parsedTransaction.interestDate())
                        .description(parsedTransaction.description())
                        .tags(Control.Option(parsedTransaction.tags())
                                .map(Collections::List)
                                .getOrSupply(() -> null)));

        long transactionId =
                creationHandler.handleCreatedEvent(new CreateTransactionCommand(transaction));

        if (parsedTransaction.category() != null) {
            this.<CategoryProvider>lookupDataProvider("CATEGORY")
                    .map(provider -> provider.lookup(parsedTransaction.category()))
                    .filter(com.jongsoft.lang.control.Optional::isPresent)
                    .map(Value::get)
                    .ifPresent(value -> LinkTransactionCommand.linkCreated(
                            transactionId,
                            LinkTransactionCommand.LinkType.CATEGORY,
                            value.getId()));
        }
        if (parsedTransaction.budget() != null) {
            this.<ExpenseProvider>lookupDataProvider("EXPENSE")
                    .map(provider -> provider.lookup(
                            filterFactory.expense().name(parsedTransaction.budget(), true)))
                    .map(ResultPage::content)
                    .map(Sequence::head)
                    .ifPresent(value -> LinkTransactionCommand.linkCreated(
                            transactionId, LinkTransactionCommand.LinkType.EXPENSE, value.getId()));
        }

        this.<ImportProvider>lookupDataProvider("IMPORT")
                .map(provider -> provider.lookup(batchImportSlug))
                .filter(com.jongsoft.lang.control.Optional::isPresent)
                .map(Value::get)
                .ifPresent(value ->
                        LinkTransactionCommand.linkCreated(transactionId, IMPORT, value.getId()));

        execution.setVariable("transactionId", transactionId);
    }

    private Account lookupAccount(DelegateExecution execution, String variableName) {
        var accountId = (Number) execution.getVariableLocal(variableName);
        return accountProvider
                .lookup(accountId.longValue())
                .getOrThrow(() ->
                        new IllegalStateException("Unable to find account with id " + accountId));
    }

    @SuppressWarnings("unchecked")
    private <T extends DataProvider<? extends Classifier>> Optional<T> lookupDataProvider(
            String type) {
        return (Optional<T>) dataProviders.stream()
                .filter(provider -> Objects.equals(provider.typeOf(), type))
                .findFirst();
    }
}
