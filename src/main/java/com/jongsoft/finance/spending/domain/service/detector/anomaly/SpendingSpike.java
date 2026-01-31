package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.InsightType;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpendingSpike implements Anomaly {

    private final FilterProvider<TransactionProvider.FilterCommand> filterFactory;
    private final TransactionProvider transactionProvider;
    private final BudgetProvider budgetProvider;

    public SpendingSpike(
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
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

            return Optional.of(new SpendingInsight(
                    InsightType.SPENDING_SPIKE,
                    getExpense(transaction),
                    getSeverityFromScore(score),
                    score,
                    transaction.getId(),
                    transaction.getDate().withDayOfMonth(1),
                    "computed.insight.spending.spike",
                    Map.of(
                            "current_month_total", currentMonthTotal,
                            "avg_monthly_spending", avgMonthlySpending,
                            "percent_increase", percentIncrease)));
        }

        return Optional.empty();
    }

    public Map<String, Double> computeSpendingPerMonth(
            Transaction transaction, int lastNumberOfMonths) {
        var expense = budgetProvider
                .lookup(transaction.getDate().getYear(), transaction.getDate().getMonthValue())
                .stream()
                .flatMap(b -> b.getExpenses().stream())
                .filter(e -> e.getName().equalsIgnoreCase(getExpense(transaction)))
                .findFirst()
                .orElseThrow();
        var filter = filterFactory
                .create()
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
