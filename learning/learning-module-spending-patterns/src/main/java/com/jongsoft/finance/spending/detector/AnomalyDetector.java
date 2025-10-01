package com.jongsoft.finance.spending.detector;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.spending.Detector;
import com.jongsoft.finance.spending.SpendingAnalyticsEnabled;
import com.jongsoft.finance.spending.detector.anomaly.*;
import com.jongsoft.lang.Dates;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@SpendingAnalyticsEnabled
class AnomalyDetector implements Detector<SpendingInsight> {

    private final TransactionProvider transactionProvider;
    private final FilterFactory filterFactory;
    private final List<Anomaly> anomalies;

    private final ThreadLocal<UserCategoryStatistics> userCategoryStatistics = new ThreadLocal<>();

    AnomalyDetector(
            TransactionProvider transactionProvider,
            FilterFactory filterFactory,
            BudgetProvider budgetProvider) {
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
        this.anomalies =
                List.of(
                        new UnusualAmount(),
                        new UnusualFrequency(transactionProvider, filterFactory, budgetProvider),
                        new SpendingSpike(filterFactory, transactionProvider, budgetProvider),
                        new UnusualMerchant());
    }

    @Override
    public boolean readyForAnalysis() {
        return true;
    }

    @Override
    public void updateBaseline(YearMonth forMonth) {
        LocalDate startDate = forMonth.minusMonths(12).atDay(1);
        userCategoryStatistics.set(
                new UserCategoryStatistics(
                        new UserCategoryStatistics.BudgetStatisticsMap(),
                        new UserCategoryStatistics.BudgetStatisticsMap(),
                        new HashMap<>()));

        log.debug("Updating baseline for anomaly detection");
        var transactionsPerBudget =
                transactionProvider
                        .lookup(
                                filterFactory
                                        .transaction()
                                        .ownAccounts()
                                        .range(Dates.range(startDate, LocalDate.now())))
                        .content()
                        .stream()
                        .filter(t -> t.getBudget() != null)
                        .collect(Collectors.groupingBy(Transaction::getBudget));
        for (var budgetTransactions : transactionsPerBudget.entrySet()) {
            var budget = budgetTransactions.getKey();
            var amountPerBudget =
                    userCategoryStatistics
                            .get()
                            .amounts()
                            .computeIfAbsent(budget, ignored -> new DescriptiveStatistics());
            for (var t : budgetTransactions.getValue()) {
                amountPerBudget.addValue(t.computeAmount(t.computeTo()));
            }

            var frequency =
                    userCategoryStatistics
                            .get()
                            .frequencies()
                            .computeIfAbsent(budget, ignored -> new DescriptiveStatistics());
            Map<String, Long> transactionsByMonth =
                    budgetTransactions.getValue().stream()
                            .collect(
                                    Collectors.groupingBy(
                                            t ->
                                                    t.getDate().getYear()
                                                            + "-"
                                                            + t.getDate().getMonthValue(),
                                            Collectors.counting()));
            for (Long count : transactionsByMonth.values()) {
                frequency.addValue(count);
            }

            Set<String> merchants =
                    budgetTransactions.getValue().stream()
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
    public List<SpendingInsight> detect(Transaction transaction) {
        var userStatistics = userCategoryStatistics.get();
        if (userStatistics == null
                || transaction.getBudget() == null
                || !userStatistics.amounts().containsKey(transaction.getBudget())) {
            return List.of();
        }

        return anomalies.stream()
                .map(anomaly -> anomaly.detect(transaction, userStatistics))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
