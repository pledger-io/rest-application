package com.jongsoft.finance.spending.domain.service.detector;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.configuration.SpendingAnalysisConfiguration;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.domain.service.SpendingAnalyticsEnabled;
import com.jongsoft.finance.spending.domain.service.detector.anomaly.*;
import com.jongsoft.lang.Dates;

import jakarta.inject.Singleton;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@SpendingAnalyticsEnabled
class AnomalyDetector implements Detector<SpendingInsight> {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(AnomalyDetector.class);
    private final TransactionProvider transactionProvider;
    private final FilterProvider<TransactionProvider.FilterCommand> filterFactory;
    private final SpendingAnalysisConfiguration settings;
    private final List<Anomaly> transactionAnomalies;
    private final List<MonthAnomaly> monthAnomalies;

    private final ThreadLocal<UserCategoryStatistics> userCategoryStatistics = new ThreadLocal<>();

    AnomalyDetector(
            TransactionProvider transactionProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
            SpendingAnalysisConfiguration settings) {
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
        this.settings = settings;
        this.transactionAnomalies = List.of(new UnusualAmount(settings), new UnusualMerchant());
        this.monthAnomalies =
                List.of(new CategoryMonthlyDeviation(settings), new UnusualFrequency(settings));
    }

    @Override
    public boolean readyForAnalysis() {
        return true;
    }

    @Override
    public void updateBaseline(YearMonth forMonth) {
        LocalDate baselineStart =
                forMonth.minusMonths(settings.baselineMonths()).atDay(1);
        LocalDate baselineEnd = forMonth.minusMonths(1).atEndOfMonth();
        YearMonth baselineStartMonth = YearMonth.from(baselineStart);
        YearMonth baselineEndMonth = forMonth.minusMonths(1);

        userCategoryStatistics.set(new UserCategoryStatistics(settings.baselineMonths()));

        log.debug(
                "Updating baseline for anomaly detection from {} to {} (excluding analyzed month {})",
                baselineStart,
                baselineEnd,
                forMonth);

        var transactionsPerBudget = transactionProvider
                .lookup(filterFactory
                        .create()
                        .ownAccounts()
                        .range(Dates.range(baselineStart, baselineEnd)))
                .content()
                .stream()
                .filter(SpendingCategoryResolver::hasCategory)
                .collect(Collectors.groupingBy(SpendingCategoryResolver::resolve));

        for (var budgetTransactions : transactionsPerBudget.entrySet()) {
            var budget = budgetTransactions.getKey();
            var transactions = budgetTransactions.getValue();

            var amountPerBudget = userCategoryStatistics
                    .get()
                    .amounts()
                    .computeIfAbsent(budget, ignored -> new DescriptiveStatistics());
            var frequency = userCategoryStatistics
                    .get()
                    .frequencies()
                    .computeIfAbsent(budget, ignored -> new DescriptiveStatistics());
            var monthlyTotals = userCategoryStatistics
                    .get()
                    .monthlyTotals()
                    .computeIfAbsent(budget, ignored -> new DescriptiveStatistics());

            Map<String, Long> transactionsByMonth = new HashMap<>();
            Map<String, Double> totalsByMonth = new HashMap<>();

            for (var transaction : transactions) {
                double amount = transaction.computeAmount(transaction.computeTo());
                amountPerBudget.addValue(amount);

                String monthKey = monthKey(transaction.getDate());
                transactionsByMonth.merge(monthKey, 1L, Long::sum);
                totalsByMonth.merge(monthKey, amount, Double::sum);
            }

            fillMonthlyBaseline(
                    baselineStartMonth,
                    baselineEndMonth,
                    transactionsByMonth,
                    totalsByMonth,
                    frequency,
                    monthlyTotals);

            userCategoryStatistics.get().monthlyTotalsByMonthKey().put(budget, totalsByMonth);

            Set<String> merchants = transactions.stream()
                    .map(Transaction::computeTo)
                    .filter(Objects::nonNull)
                    .map(Account::getName)
                    .collect(Collectors.toSet());
            userCategoryStatistics.get().typicalMerchants().put(budget, merchants);
        }

        log.debug("Baseline update completed");
    }

    @Override
    public void analysisCompleted() {
        userCategoryStatistics.remove();
        log.debug("Analysis completed. Removed user category statistics.");
    }

    @Override
    public List<SpendingInsight> detectForMonth(
            YearMonth forMonth, List<Transaction> transactions) {
        var userStatistics = userCategoryStatistics.get();
        if (userStatistics == null) {
            return List.of();
        }

        var summariesByCategory = groupByExpense(transactions);
        return userStatistics.monthlyTotals().keySet().stream()
                .flatMap(category -> {
                    var summary =
                            summariesByCategory.getOrDefault(category, CategoryMonthSummary.EMPTY);
                    return monthAnomalies.stream()
                            .map(anomaly ->
                                    anomaly.detect(category, forMonth, summary, userStatistics))
                            .filter(Optional::isPresent)
                            .map(Optional::get);
                })
                .toList();
    }

    @Override
    public List<SpendingInsight> detect(Transaction transaction) {
        var userStatistics = userCategoryStatistics.get();
        String category = SpendingCategoryResolver.resolve(transaction);
        if (userStatistics == null
                || category == null
                || !userStatistics.amounts().containsKey(category)) {
            return List.of();
        }

        return transactionAnomalies.stream()
                .map(anomaly -> anomaly.detect(transaction, userStatistics))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static void fillMonthlyBaseline(
            YearMonth baselineStartMonth,
            YearMonth baselineEndMonth,
            Map<String, Long> transactionsByMonth,
            Map<String, Double> totalsByMonth,
            DescriptiveStatistics frequency,
            DescriptiveStatistics monthlyTotals) {
        for (YearMonth month = baselineStartMonth;
                !month.isAfter(baselineEndMonth);
                month = month.plusMonths(1)) {
            String key = monthKey(month);
            long count = transactionsByMonth.getOrDefault(key, 0L);
            double total = totalsByMonth.getOrDefault(key, 0.0);
            frequency.addValue(count);
            monthlyTotals.addValue(total);
            totalsByMonth.putIfAbsent(key, total);
        }
    }

    private Map<String, CategoryMonthSummary> groupByExpense(List<Transaction> transactions) {
        return transactions.stream()
                .filter(SpendingCategoryResolver::hasCategory)
                .collect(Collectors.groupingBy(
                        SpendingCategoryResolver::resolve,
                        Collectors.collectingAndThen(
                                Collectors.toList(), CategoryMonthSummary::from)));
    }

    private static String monthKey(LocalDate date) {
        return date.getYear() + "-" + date.getMonthValue();
    }

    private static String monthKey(YearMonth month) {
        return month.getYear() + "-" + month.getMonthValue();
    }
}
