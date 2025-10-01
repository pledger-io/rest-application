package com.jongsoft.finance.spending.detector.pattern;

import static org.slf4j.LoggerFactory.getLogger;

import com.jongsoft.finance.domain.insight.PatternType;
import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.domain.transaction.Transaction;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import org.slf4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class OccurrencePattern implements Pattern {

    private static final double PERCENTAGE_INSIDE_AVG_INTERVAL = 0.75;
    private static final int DAYS_DEVIATION_ALLOWED = 3;
    private final Logger log = getLogger(OccurrencePattern.class);

    public Optional<SpendingPattern> detect(
            Transaction transaction, List<EmbeddingMatch<TextSegment>> matches) {
        // Need at least 3 matches to establish a pattern
        if (matches.size() < 3) {
            return Optional.empty();
        }

        var dates =
                matches.stream()
                        .map(
                                match ->
                                        LocalDate.parse(
                                                Objects.requireNonNull(
                                                        match.embedded()
                                                                .metadata()
                                                                .getString("date"))))
                        .sorted()
                        .toList();

        var detected = detectMonthlyOrWeekly(computeIntervals(dates));
        if (detected != null) {
            var amounts =
                    matches.stream()
                            .map(match -> match.embedded().metadata().getDouble("amount"))
                            .filter(Objects::nonNull)
                            .toList();

            return Optional.of(
                    SpendingPattern.builder()
                            .type(detected)
                            .category(transaction.getCategory())
                            .detectedDate(transaction.getDate().withDayOfMonth(1))
                            .confidence(calculateConfidence(matches))
                            .metadata(
                                    Map.of(
                                            "frequency",
                                                    detected == PatternType.RECURRING_WEEKLY
                                                            ? "weekly"
                                                            : "monthly",
                                            "typical_amount", calculateAverage(amounts),
                                            "vector_similarity",
                                                    calculateAverageSimilarity(matches),
                                            "typical_day", getMostCommonDayOfWeek(dates)))
                            .build());
        }

        return Optional.empty();
    }

    private double calculateAverage(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double calculateConfidence(List<EmbeddingMatch<TextSegment>> matches) {
        // Calculate confidence based on number of matches and their similarity scores
        double avgSimilarity = calculateAverageSimilarity(matches);
        double matchFactor =
                Math.min(1.0, matches.size() / 10.0); // Scale based on number of matches, max at 10

        return avgSimilarity * 0.7 + matchFactor * 0.3; // Weighted combination
    }

    private double calculateAverageSimilarity(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream().mapToDouble(EmbeddingMatch::score).average().orElse(0);
    }

    private DayOfWeek getMostCommonDayOfWeek(List<LocalDate> dates) {
        Map<DayOfWeek, Long> dayCount =
                dates.stream()
                        .collect(
                                Collectors.groupingBy(
                                        LocalDate::getDayOfWeek, Collectors.counting()));

        return dayCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.MONDAY);
    }

    private PatternType detectMonthlyOrWeekly(List<Number> intervals) {
        if (intervals.isEmpty()) return null;

        var avgInterval = (int) intervals.stream().mapToLong(Number::longValue).average().orElse(0);

        // Check for monthly pattern first (more specific check)
        if (avgInterval >= 28 && avgInterval <= 31) {
            // For monthly patterns, we need to be more lenient because months have different
            // lengths
            // Check if most intervals are between 28 and 31 days
            var monthlyIntervals =
                    intervals.stream()
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

        // Check for weekly pattern
        var numberWithinAvg =
                intervals.stream()
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
            intervals.add(ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i)));
        }
        return intervals;
    }
}
