package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.importer.api.TransactionDTO;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

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

  CreateTransactionDelegate(
      AccountProvider accountProvider, TransactionCreationHandler creationHandler) {
    this.accountProvider = accountProvider;
    this.creationHandler = creationHandler;
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
        toAccount, parsedTransaction.amount(), type, t -> t.currency(targetAccount.getCurrency())
            .date(parsedTransaction.transactionDate())
            .bookDate(parsedTransaction.bookDate())
            .interestDate(parsedTransaction.interestDate())
            .description(parsedTransaction.description())
            .category(parsedTransaction.category())
            .budget(parsedTransaction.budget())
            .tags(Control.Option(parsedTransaction.tags())
                .map(Collections::List)
                .getOrSupply(() -> null))
            .importSlug(batchImportSlug));

    long transactionId =
        creationHandler.handleCreatedEvent(new CreateTransactionCommand(transaction));

    execution.setVariable("transactionId", transactionId);
  }

  private Account lookupAccount(DelegateExecution execution, String variableName) {
    var accountId = (Number) execution.getVariableLocal(variableName);
    return accountProvider
        .lookup(accountId.longValue())
        .getOrThrow(() -> new IllegalStateException("Unable to find account with id " + accountId));
  }
}
