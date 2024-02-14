package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.AccountProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;

@Slf4j
@Singleton
public class CreateTransactionDelegate implements JavaDelegate {

    private final AccountProvider accountProvider;
    private final TransactionCreationHandler creationHandler;

    CreateTransactionDelegate(AccountProvider accountProvider, TransactionCreationHandler creationHandler) {
        this.accountProvider = accountProvider;
        this.creationHandler = creationHandler;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var batchImportSlug = (String) execution.getVariable("importJobSlug");
        var parsedTransaction = (ParsedTransaction) execution.getVariableLocal("transaction");
        var toAccount = lookupAccount(execution, "accountId");
        var targetAccount = lookupAccount(execution, "targetAccount");

        log.debug("{}: Creating transaction into {} from {} with amount {}",
                execution.getCurrentActivityName(),
                targetAccount.getName(),
                toAccount.getName(),
                parsedTransaction.getAmount());

        Transaction transaction = targetAccount.createTransaction(
                toAccount,
                parsedTransaction.getAmount(),
                parsedTransaction.getType(),
                t -> t.currency(targetAccount.getCurrency())
                        .date(parsedTransaction.getTransactionDate())
                        .bookDate(parsedTransaction.getBookDate())
                        .interestDate(parsedTransaction.getInterestDate())
                        .description(parsedTransaction.getDescription())
                        .importSlug(batchImportSlug));

        long transactionId = creationHandler.handleCreatedEvent(new CreateTransactionCommand(transaction));

        execution.setVariable("transactionId", transactionId);
    }

    private Account lookupAccount(DelegateExecution execution, String variableName) {
        var accountId = execution.<LongValue>getVariableLocalTyped(variableName).getValue();
        return accountProvider.lookup(accountId)
                .getOrThrow(() -> new IllegalStateException("Unable to find account with id " + accountId));
    }

}
