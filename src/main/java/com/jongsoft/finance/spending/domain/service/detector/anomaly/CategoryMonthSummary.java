package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import com.jongsoft.finance.banking.domain.model.Transaction;

import java.util.Comparator;
import java.util.List;

/** Aggregated spending data for one expense category within the analyzed month. */
public record CategoryMonthSummary(
        double totalAmount, long transactionCount, List<Long> topContributingTransactionIds) {

    public static final CategoryMonthSummary EMPTY = new CategoryMonthSummary(0.0, 0, List.of());

    public static CategoryMonthSummary from(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return EMPTY;
        }

        double total = transactions.stream()
                .mapToDouble(t -> t.computeAmount(t.computeTo()))
                .sum();
        var topIds = transactions.stream()
                .sorted(Comparator.comparingDouble(
                                (Transaction t) -> t.computeAmount(t.computeTo()))
                        .reversed())
                .limit(3)
                .map(Transaction::getId)
                .toList();

        return new CategoryMonthSummary(total, transactions.size(), topIds);
    }
}
