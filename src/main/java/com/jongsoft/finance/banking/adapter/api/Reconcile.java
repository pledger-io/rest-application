package com.jongsoft.finance.banking.adapter.api;

import com.jongsoft.finance.banking.domain.model.AccountReconciliation;

import java.util.List;
import java.util.Optional;

public interface Reconcile {

    /**
     * Reconciles an account's financial data for a specific year based on the provided
     * starting and ending balances.
     *
     * @param accountId The unique identifier of the account to be reconciled.
     * @param year The fiscal year for which the reconciliation is performed.
     * @param startBalance The starting balance of the account for the specified year.
     * @param endBalance The ending balance of the account for the specified year.
     * @return An {@code Optional} containing the {@code AccountReconciliation} details if the
     *         reconciliation failed, or an empty {@code Optional} if it was successful.
     */
    Optional<AccountReconciliation> reconcile(
            Long accountId, int year, double startBalance, double endBalance);

    /**
     * Fetches a list of account reconciliations for the specified account ID.
     *
     * @param accountId The unique identifier of the account for which reconciliations
     *                  are to be retrieved.
     * @return A list of {@code AccountReconciliation} records associated with the
     *         specified account ID.
     */
    List<AccountReconciliation> fetchAccountsReconcile(Long accountId);
}
