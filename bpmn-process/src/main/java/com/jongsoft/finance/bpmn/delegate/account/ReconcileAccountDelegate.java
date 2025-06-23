package com.jongsoft.finance.bpmn.delegate.account;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.AccountProvider;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.StringValue;

/**
 * This delegate will create a reconciliation transaction into a selected account. This can be used
 * to correct missing funds in an account.
 *
 * <p>This delegate expects the following variables to be present:
 *
 * <ul>
 *   <li>accountId, the account we are creating a transaction for
 *   <li>amount, the amount that is either short (in case of a negative number) or too much in the
 *       account
 *   <li>bookDate, the date the transaction should be created on
 * </ul>
 */
@Slf4j
@Singleton
public class ReconcileAccountDelegate implements JavaDelegate, JavaBean {

  private final AccountProvider accountProvider;
  private final TransactionCreationHandler creationHandler;

  ReconcileAccountDelegate(
      AccountProvider accountProvider, TransactionCreationHandler creationHandler) {
    this.accountProvider = accountProvider;
    this.creationHandler = creationHandler;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var accountId = ((Number) execution.getVariableLocal("accountId")).longValue();
    var amount = execution.<ObjectValue>getVariableLocalTyped("amount").getValue(BigDecimal.class);
    var isoBookDate = execution.<StringValue>getVariableLocalTyped("bookDate").getValue();

    var transactionDate = LocalDate.parse(isoBookDate).minusDays(1);
    log.debug(
        "{}: Reconciling account {} for book date {} with amount {}",
        execution.getCurrentActivityName(),
        accountId,
        transactionDate,
        amount);

    Account toReconcile = accountProvider.lookup(accountId).get();
    Account reconcileAccount = accountProvider
        .lookup(SystemAccountTypes.RECONCILE)
        .getOrThrow(() -> StatusException.badRequest("Reconcile account not found"));

    Transaction.Type type =
        amount.compareTo(BigDecimal.ZERO) >= 0 ? Transaction.Type.CREDIT : Transaction.Type.DEBIT;
    Transaction transaction = toReconcile.createTransaction(
        reconcileAccount, amount.abs().doubleValue(), type, t -> t.description(
                "Reconcile transaction")
            .currency(toReconcile.getCurrency())
            .date(transactionDate));

    creationHandler.handleCreatedEvent(new CreateTransactionCommand(transaction));
  }
}
