package com.jongsoft.finance.spending.scheduler;

import static com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand.LinkType.EXPENSE;

import com.jongsoft.finance.domain.insight.Insight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.commands.insight.CleanInsightsForMonth;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.spending.Detector;
import com.jongsoft.finance.spending.SpendingAnalyticsEnabled;
import com.jongsoft.lang.Dates;

import io.micrometer.core.annotation.Timed;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Singleton
@SpendingAnalyticsEnabled
class AnalysisRunner {
    private final List<Detector<?>> transactionDetectors;
    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;

    AnalysisRunner(
            List<Detector<?>> transactionDetectors,
            FilterFactory filterFactory,
            TransactionProvider transactionProvider) {
        this.transactionDetectors = transactionDetectors;
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
    }

    @Transactional
    @Timed("insight.monthly.analysis")
    public boolean analyzeForUser(YearMonth month) {
        if (!transactionDetectors.stream().allMatch(Detector::readyForAnalysis)) {
            log.debug("Not all transaction detectors are ready for analysis. Skipping analysis.");
            return false;
        }

        log.info("Starting monthly spending analysis for {}.", month);
        try {
            CleanInsightsForMonth.cleanInsightsForMonth(month);
            var transactionFilter = filterFactory
                    .transaction()
                    .range(Dates.range(month.atDay(1), month.atEndOfMonth()))
                    .ownAccounts();

            var transactionInMonth =
                    transactionProvider.lookup(transactionFilter).content();
            if (transactionInMonth.isEmpty()) {
                log.trace("No transactions found for {}, skipping analysis.", month);
                return false;
            }

            log.debug("Retrieved {} transactions for {}.", transactionInMonth.size(), month);
            transactionDetectors.forEach(detector -> detector.updateBaseline(month));
            transactionInMonth.stream()
                    .flatMap(t -> processTransaction(t).stream())
                    .distinct()
                    .forEach(Insight::signal);
            log.debug("Completed monthly spending analysis for {}.", month);
        } catch (Exception e) {
            log.error(
                    "Error occurred while processing monthly spending analysis for {}.", month, e);
            return false;
        } finally {
            transactionDetectors.forEach(Detector::analysisCompleted);
        }

        return true;
    }

    private List<? extends Insight> processTransaction(Transaction transaction) {
        if (!transaction.getMetadata().containsKey(EXPENSE.name())) {
            return List.of(); // Skip transactions without a category
        }

        var insightList = new java.util.ArrayList<Insight>();
        for (var detector : transactionDetectors) {
            log.trace(
                    "Processing transaction {} with detector {}.",
                    transaction,
                    detector.getClass().getSimpleName());
            insightList.addAll(detector.detect(transaction));
        }
        return insightList;
    }
}
