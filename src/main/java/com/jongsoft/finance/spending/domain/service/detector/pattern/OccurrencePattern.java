package com.jongsoft.finance.spending.domain.service.detector.pattern;

import static org.slf4j.LoggerFactory.getLogger;

import com.jongsoft.finance.spending.domain.model.SpendingPattern;
import com.jongsoft.finance.spending.types.PatternType;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import org.slf4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class OccurrencePattern implements Pattern {

    private static final double PERCENTAGE_INSIDE_AVG_INTERVAL = 0.75;
    private static final int DAYS_DEVIATION_ALLOWED = 3;

    private final Logger log = getLogger(OccurrencePattern.class);

    @Override
    public Optional<SpendingPattern> detect(
            String category, YearMonth forMonth, PatternMonthContext context) {
        var dates = collectDates(context);
        if (dates.size() < 3) {
            return Optional.empty();
        }

        var detected = detectMonthlyOrWeekly(computeIntervals(dates));
        if (detected == null) {
            return Optional.empty();
        }

        var amounts = context.historicMatches().stream()
                .map(match -> match.embedded().metadata().getDouble("amount"))
                .filter(Objects::nonNull)
                .toList();

        var metadata = new HashMap<String, Object>();
        metadata.put("frequency", detected == PatternType.RECURRING_WEEKLY ? "weekly" : "monthly");
        metadata.put("typical_amount", calculateAverage(amounts));
        metadata.put("vector_similarity", calculateAverageSimilarity(context.historicMatches()));
        metadata.put("typical_day", getMostCommonDayOfWeek(dates).toString());
        metadata.put("lookback_months", context.lookbackMonths());
        metadata.put("match_count", context.historicMatches().size());

        return Optional.of(new SpendingPattern(
                detected,
                category,
                calculateConfidence(context.historicMatches()),
                forMonth.atDay(1),
                metadata));
    }

    private List<LocalDate> collectDates(PatternMonthContext context) {
        var dates = new ArrayList<LocalDate>();
        context.monthTransactions().forEach(t -> dates.add(t.getDate()));
        context.historicMatches().stream()
                .map(match -> LocalDate.parse(
                        Objects.requireNonNull(match.embedded().metadata().getString("date"))))
                .forEach(dates::add);
        return dates.stream().sorted().distinct().toList();
    }

    private double calculateAverage(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double calculateConfidence(List<EmbeddingMatch<TextSegment>> matches) {
        double avgSimilarity = calculateAverageSimilarity(matches);
        double matchFactor = Math.min(1.0, matches.size() / 10.0);
        return avgSimilarity * 0.7 + matchFactor * 0.3;
    }

    private double calculateAverageSimilarity(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream().mapToDouble(EmbeddingMatch::score).average().orElse(0);
    }

    private DayOfWeek getMostCommonDayOfWeek(List<LocalDate> dates) {
        Map<DayOfWeek, Long> dayCount = dates.stream()
                .collect(Collectors.groupingBy(LocalDate::getDayOfWeek, Collectors.counting()));

        return dayCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.MONDAY);
    }

    private PatternType detectMonthlyOrWeekly(List<Number> intervals) {
        if (intervals.isEmpty()) {
            return null;
        }

        var avgInterval =
                (int) intervals.stream().mapToLong(Number::longValue).average().orElse(0);

        if (avgInterval >= 28 && avgInterval <= 31) {
            var monthlyIntervals = intervals.stream()
                    .mapToLong(Number::longValue)
                    .filter(i -> i >= 28 && i <= 31)
                    .count();
            boolean isMonthly =
                    monthlyIntervals >= intervals.size() * PERCENTAGE_INSIDE_AVG_INTERVAL;

            log.trace(
                    "Average interval: {}, monthly intervals: {}, is monthly: {}.",
                    avgInterval,
                    monthlyIntervals,
                    isMonthly);
            if (isMonthly) {
                return PatternType.RECURRING_MONTHLY;
            }
        }

        var numberWithinAvg = intervals.stream()
                .mapToLong(Number::longValue)
                .filter(i -> Math.abs(i - avgInterval) <= DAYS_DEVIATION_ALLOWED)
                .count();
        boolean isConsistent = numberWithinAvg >= intervals.size() * PERCENTAGE_INSIDE_AVG_INTERVAL;

        log.trace(
                "Average interval: {}, number within avg: {}, consistent: {}.",
                avgInterval,
                numberWithinAvg,
                isConsistent);
        if (!isConsistent) {
            return null;
        }

        if (avgInterval >= 7 && avgInterval <= 8) {
            return PatternType.RECURRING_WEEKLY;
        }

        return null;
    }

    private List<Number> computeIntervals(List<LocalDate> dates) {
        List<Number> intervals = new ArrayList<>();
        for (int i = 1; i < dates.size(); i++) {
            intervals.add(
                    java.time.temporal.ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i)));
        }
        return intervals;
    }
}
