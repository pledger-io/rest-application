package com.jongsoft.finance.bpmn.delegate.account;

import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.AccountProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ReconcileAccountDelegate implements JavaDelegate {

    private final AccountProvider accountProvider;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long accountId = ((Number) execution.getVariableLocal("accountId")).longValue();
        BigDecimal amount = (BigDecimal) execution.getVariableLocal("amount");
        String isoBookDate = execution.<StringValue>getVariableLocalTyped("bookDate").getValue();

        Account toReconcile = accountProvider.lookup(accountId).get();

        log.debug("{}: Reconciling account {} for book date {} with amount {}", execution.getCurrentActivityName(),
                toReconcile.getName(), isoBookDate, amount);

        Account reconcileAccount = accountProvider.lookup(SystemAccountTypes.RECONCILE)
                .block(Duration.of(500, ChronoUnit.MILLIS));

        var transactionDate = LocalDate.parse(isoBookDate);
        Transaction.Type type = amount.compareTo(BigDecimal.ZERO) >= 0 ? Transaction.Type.CREDIT : Transaction.Type.DEBIT;
        Transaction transaction = toReconcile.createTransaction(
                reconcileAccount,
                amount.abs().doubleValue(),
                type,
                t -> t.description("Reconcile transaction")
                        .currency(toReconcile.getCurrency())
                        .date(transactionDate));

        transaction.register();
    }

}
