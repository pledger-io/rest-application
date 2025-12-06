package com.jongsoft.finance.spending.detector.pattern;

import com.jongsoft.finance.domain.insight.PatternType;
import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.domain.transaction.Transaction;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SeasonalPattern implements Pattern {

    @Override
    public Optional<SpendingPattern> detect(
            Transaction transaction, List<EmbeddingMatch<TextSegment>> matches) {
        if (matches.isEmpty()) {
            return Optional.empty();
        }

        int currentMonth = transaction.getDate().getMonthValue();
        var transactionsByMonth = matches.stream()
                .map(match -> LocalDate.parse(
                        Objects.requireNonNull(match.embedded().metadata().getString("date"))))
                .map(LocalDate::getMonthValue)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (transactionsByMonth.isEmpty()) {
            return Optional.empty();
        }

        var numberInMonth = transactionsByMonth.getOrDefault(currentMonth, 0L);
        var avgPerMonth = matches.size() / transactionsByMonth.size();
        if (isSignificantlyMoreThanAverage(numberInMonth, avgPerMonth)) {
            return Optional.of(SpendingPattern.builder()
                    .type(PatternType.SEASONAL)
                    .category(getExpense(transaction))
                    .detectedDate(transaction.getDate().withDayOfMonth(1))
                    .confidence(.75)
                    .metadata(Map.of("season", getCurrentSeason(transaction.getDate())))
                    .build());
        }

        return Optional.empty();
    }

    private boolean isSignificantlyMoreThanAverage(long numberInMonth, long avgPerMonth) {
        return numberInMonth >= avgPerMonth * 2.0;
    }

    private String getCurrentSeason(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 3 && month <= 5) {
            return "Spring";
        } else if (month >= 6 && month <= 8) {
            return "Summer";
        } else if (month >= 9 && month <= 11) {
            return "Fall";
        } else {
            return "Winter";
        }
    }
}
