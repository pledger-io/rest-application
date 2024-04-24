package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.serialized.TransactionJson;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
@Singleton
public class ImportTransactionJsonDelegate implements JavaDelegate, JavaBean {

    private final AccountProvider accountProvider;
    private final TransactionCreationHandler creationHandler;

    public ImportTransactionJsonDelegate(AccountProvider accountProvider, TransactionCreationHandler creationHandler) {
        this.accountProvider = accountProvider;
        this.creationHandler = creationHandler;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var transaction = (TransactionJson) execution.getVariableLocal("transaction");

        log.debug("{}: Importing a transaction from '{}' to '{}'.",
                execution.getCurrentActivityName(),
                transaction.getFromAccount(),
                transaction.getToAccount());

        var fromAccount = accountProvider.lookup(transaction.getFromAccount())
                .getOrThrow(() ->
                        new IllegalStateException(
                                "Unable to find account with name " + transaction.getFromAccount()));
        var toAccount = accountProvider.lookup(transaction.getToAccount())
                .getOrThrow(() ->
                        new IllegalStateException(
                                "Unable to find account with name " + transaction.getToAccount()));

        var created = fromAccount.createTransaction(
                toAccount,
                transaction.getAmount(),
                Transaction.Type.CREDIT,
                t -> t.currency(transaction.getCurrency())
                        .date(transaction.getDate())
                        .bookDate(transaction.getBookDate())
                        .interestDate(transaction.getInterestDate())
                        .description(transaction.getDescription()));

        long transactionId = creationHandler.handleCreatedEvent(new CreateTransactionCommand(created));
        execution.setVariable("transactionId", transactionId);
    }
}
