package com.jongsoft.finance.spending.domain.service.detector;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
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
    private final List<Anomaly> anomalies;

    private final ThreadLocal<UserCategoryStatistics> userCategoryStatistics = new ThreadLocal<>();

    AnomalyDetector(
            TransactionProvider transactionProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
            BudgetProvider budgetProvider) {
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
        this.anomalies = List.of(
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
        userCategoryStatistics.set(new UserCategoryStatistics(
                new UserCategoryStatistics.BudgetStatisticsMap(),
                new UserCategoryStatistics.BudgetStatisticsMap(),
                new HashMap<>()));

        log.debug("Updating baseline for anomaly detection");
        var transactionsPerBudget = transactionProvider
                .lookup(filterFactory
                        .create()
                        .ownAccounts()
                        .range(Dates.range(startDate, LocalDate.now())))
                .content()
                .stream()
                .filter(t -> t.getMetadata().containsKey("EXPENSE"))
                .collect(Collectors.groupingBy(
                        transaction -> transaction.getMetadata().get("EXPENSE").toString()));
        for (var budgetTransactions : transactionsPerBudget.entrySet()) {
            var budget = budgetTransactions.getKey();
            var amountPerBudget = userCategoryStatistics
                    .get()
                    .amounts()
                    .computeIfAbsent(budget, ignored -> new DescriptiveStatistics());
            for (var t : budgetTransactions.getValue()) {
                amountPerBudget.addValue(t.computeAmount(t.computeTo()));
            }

            var frequency = userCategoryStatistics
                    .get()
                    .frequencies()
                    .computeIfAbsent(budget, ignored -> new DescriptiveStatistics());
            Map<String, Long> transactionsByMonth = budgetTransactions.getValue().stream()
                    .collect(Collectors.groupingBy(
                            t -> t.getDate().getYear() + "-" + t.getDate().getMonthValue(),
                            Collectors.counting()));
            for (Long count : transactionsByMonth.values()) {
                frequency.addValue(count);
            }

            Set<String> merchants = budgetTransactions.getValue().stream()
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
        var expense = (EntityRef.NamedEntity) transaction.getMetadata().get("EXPENSE");
        if (userStatistics == null
                || expense == null
                || !userStatistics.amounts().containsKey(expense.name())) {
            return List.of();
        }

        return anomalies.stream()
                .map(anomaly -> anomaly.detect(transaction, userStatistics))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
