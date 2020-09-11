package com.jongsoft.finance.bpmn.delegate.account;

import java.time.LocalDate;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.transaction.Transaction;

import lombok.extern.slf4j.Slf4j;

/**
 * This delegate will create a reconciliation transaction into a selected account. This can be used to correct missing
 * funds in an account.
 *
 * <p>
 * This delegate expects the following variables to be present:
 * </p>
 * <ul>
 *     <li>accountId, the account we are creating a transaction for</li>
 *     <li>amount, the amount that is either short (in case of a negative number) or too much in the account</li>
 *     <li>bookDate, the date the transaction should be created on</li>
 * </ul>
 */
@Slf4j
@Singleton
public class ReconcileAccountDelegate implements JavaDelegate {

    private final AccountProvider accountProvider;

    @Inject
    public ReconcileAccountDelegate(AccountProvider accountProvider) {
        this.accountProvider = accountProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long accountId = ((Number) execution.getVariableLocal("accountId")).longValue();
        Double amount = execution.<DoubleValue>getVariableLocalTyped("amount").getValue();
        String isoBookDate = execution.<StringValue>getVariableLocalTyped("bookDate").getValue();

        Account toReconcile = accountProvider.lookup(accountId).get();

        log.debug("{}: Reconciling account {} for book date {} with amount {}", execution.getCurrentActivityName(),
                toReconcile.getName(), isoBookDate, amount);

        Account reconcileAccount = accountProvider.lookup(SystemAccountTypes.RECONCILE)
                .get();

        var transactionDate = LocalDate.parse(isoBookDate);
        Transaction.Type type = amount >= 0 ? Transaction.Type.CREDIT : Transaction.Type.DEBIT;
        Transaction transaction = toReconcile.createTransaction(
                reconcileAccount,
                Math.abs(amount),
                type,
                t -> t.description("Reconcile transaction")
                        .currency(toReconcile.getCurrency())
                        .date(transactionDate));

        transaction.register();
    }

}
