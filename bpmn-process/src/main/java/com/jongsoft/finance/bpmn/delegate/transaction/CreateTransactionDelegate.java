package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.importer.api.TransactionDTO;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.AccountProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.LongValue;

/**
 * Delegate for creating a transaction in the system.
 * <p>
 *     This delegate is responsible for creating a transaction in the system. It is used in the BPMN process to
 *     create a transaction from the parsed transaction data.
    * </p>
 * <p>
 *     The delegate expects the following variables to be present in the execution:
 *     <ul>
 *         <li>transaction: The parsed transaction data from the import job</li>
 *         <li>accountId: The ID of the account to create the transaction in</li>
 *         <li>targetAccount: The ID of the account to create the transaction for</li>
 *         <li>importJobSlug: The slug of the import job that the transaction is part of</li>
 *      </ul>
 *      The delegate will create the transaction in the target account and set the {@code transactionId} in the execution.
 * </p>
 */
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
        var parsedTransaction = (TransactionDTO) execution.getVariableLocal("transaction");
        var toAccount = lookupAccount(execution, "accountId");
        var targetAccount = lookupAccount(execution, "targetAccount");

        log.debug("{}: Creating transaction into {} from {} with amount {}",
                execution.getCurrentActivityName(),
                targetAccount.getName(),
                toAccount.getName(),
                parsedTransaction.amount());

        var type = switch (parsedTransaction.type()) {
            case DEBIT -> Transaction.Type.DEBIT;
            case CREDIT -> Transaction.Type.CREDIT;
            case TRANSFER -> Transaction.Type.TRANSFER;
        };

        Transaction transaction = targetAccount.createTransaction(
                toAccount,
                parsedTransaction.amount(),
                type,
                t -> t.currency(targetAccount.getCurrency())
                        .date(parsedTransaction.transactionDate())
                        .bookDate(parsedTransaction.bookDate())
                        .interestDate(parsedTransaction.interestDate())
                        .description(parsedTransaction.description())
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
