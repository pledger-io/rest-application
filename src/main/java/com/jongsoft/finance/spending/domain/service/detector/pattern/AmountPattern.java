package com.jongsoft.finance.spending.domain.service.detector.pattern;

import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.spending.domain.model.SpendingPattern;
import com.jongsoft.finance.spending.types.PatternType;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/** Detects gradual spending trends using monthly category totals across the lookback window. */
public class AmountPattern implements Pattern {

    private static final double TREND_THRESHOLD = 0.15;
    private static final int MIN_MONTHS = 4;
    private static final int RECENT_MONTHS = 3;

    @Override
    public Optional<SpendingPattern> detect(
            String category, YearMonth forMonth, PatternMonthContext context) {
        Map<YearMonth, Double> monthlyTotals = buildMonthlyTotals(context, forMonth);
        if (monthlyTotals.size() < MIN_MONTHS) {
            return Optional.empty();
        }

        List<Double> totalsInOrder = monthlyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();

        int recentCount = Math.min(RECENT_MONTHS, totalsInOrder.size() / 2);
        if (recentCount == 0 || totalsInOrder.size() <= recentCount) {
            return Optional.empty();
        }

        double recentAvg = average(
                totalsInOrder.subList(totalsInOrder.size() - recentCount, totalsInOrder.size()));
        double priorAvg = average(totalsInOrder.subList(0, totalsInOrder.size() - recentCount));

        if (priorAvg == 0.0) {
            return Optional.empty();
        }

        double percentChange = (recentAvg - priorAvg) / priorAvg;
        PatternType patternType = null;
        if (percentChange > TREND_THRESHOLD) {
            patternType = PatternType.INCREASING_TREND;
        } else if (percentChange < -TREND_THRESHOLD) {
            patternType = PatternType.DECREASING_TREND;
        }

        if (patternType == null) {
            return Optional.empty();
        }

        double currentMonthTotal = monthlyTotals.getOrDefault(forMonth, 0.0);
        var metadata = new HashMap<String, Object>();
        metadata.put("typical_amount", priorAvg);
        metadata.put("current_month_total", currentMonthTotal);
        metadata.put("recent_months_avg", recentAvg);
        metadata.put("percent_change", percentChange * 100.0);
        metadata.put("lookback_months", context.lookbackMonths());
        metadata.put("months_analyzed", monthlyTotals.size());

        return Optional.of(
                new SpendingPattern(patternType, category, 0.85, forMonth.atDay(1), metadata));
    }

    private Map<YearMonth, Double> buildMonthlyTotals(
            PatternMonthContext context, YearMonth forMonth) {
        Map<YearMonth, Double> monthlyTotals = new TreeMap<>();

        for (EmbeddingMatch<TextSegment> match : context.historicMatches()) {
            LocalDate date = LocalDate.parse(
                    Objects.requireNonNull(match.embedded().metadata().getString("date")));
            Double amount = match.embedded().metadata().getDouble("amount");
            if (amount != null) {
                monthlyTotals.merge(YearMonth.from(date), amount, Double::sum);
            }
        }

        for (Transaction transaction : context.monthTransactions()) {
            double amount = transaction.computeAmount(transaction.computeTo());
            monthlyTotals.merge(YearMonth.from(transaction.getDate()), amount, Double::sum);
        }

        return monthlyTotals;
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
