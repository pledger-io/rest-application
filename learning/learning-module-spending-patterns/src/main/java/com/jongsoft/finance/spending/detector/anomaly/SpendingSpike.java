package com.jongsoft.finance.spending.detector.anomaly;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.insight.InsightType;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpendingSpike implements Anomaly {

    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;
    private final BudgetProvider budgetProvider;

    public SpendingSpike(
            FilterFactory filterFactory,
            TransactionProvider transactionProvider,
            BudgetProvider budgetProvider) {
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
        this.budgetProvider = budgetProvider;
    }

    @Override
    public Optional<SpendingInsight> detect(
            Transaction transaction, UserCategoryStatistics statistics) {
        var monthlyMap = computeSpendingPerMonth(transaction, 4);
        var avgMonthlySpending = monthlyMap.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        String currentMonth =
                transaction.getDate().getYear() + "-" + transaction.getDate().getMonthValue();
        double currentMonthTotal = monthlyMap.getOrDefault(currentMonth, 0.0);
        if (currentMonthTotal > avgMonthlySpending * 1.5) {
            double percentIncrease = (currentMonthTotal - avgMonthlySpending) / avgMonthlySpending;
            double score = Math.min(1.0, percentIncrease);

            return Optional.of(SpendingInsight.builder()
                    .type(InsightType.SPENDING_SPIKE)
                    .category(transaction.getBudget())
                    .severity(getSeverityFromScore(score))
                    .score(score)
                    .detectedDate(transaction.getDate().withDayOfMonth(1))
                    .message("computed.insight.spending.spike")
                    .metadata(Map.of(
                            "current_month_total", currentMonthTotal,
                            "avg_monthly_spending", avgMonthlySpending,
                            "percent_increase", percentIncrease))
                    .build());
        }

        return Optional.empty();
    }

    public Map<String, Double> computeSpendingPerMonth(
            Transaction transaction, int lastNumberOfMonths) {
        var expense = budgetProvider
                .lookup(transaction.getDate().getYear(), transaction.getDate().getMonthValue())
                .stream()
                .flatMap(b -> b.getExpenses().stream())
                .filter(e -> e.getName().equalsIgnoreCase(transaction.getBudget()))
                .findFirst()
                .orElseThrow();
        var filter = filterFactory
                .transaction()
                .ownAccounts()
                .expenses(Collections.List(new EntityRef(expense.getId())));

        var currentMonth = transaction.getDate().withDayOfMonth(1);
        var monthlyMap = new HashMap<String, Double>();
        for (int i = 0; i < lastNumberOfMonths; i++) {
            var startDate = transaction.getDate().minusMonths(i);
            var dateRange = Dates.range(currentMonth, ChronoUnit.MONTHS);

            var computedBalance = transactionProvider.balance(filter.range(dateRange));
            computedBalance.ifPresent(amount -> monthlyMap.put(
                    startDate.getYear() + "-" + startDate.getMonthValue(),
                    Math.abs(amount.doubleValue())));
            currentMonth = currentMonth.minusMonths(1);
        }

        return monthlyMap;
    }
}
