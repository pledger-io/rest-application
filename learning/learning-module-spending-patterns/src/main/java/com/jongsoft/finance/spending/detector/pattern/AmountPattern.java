package com.jongsoft.finance.spending.detector.pattern;

import com.jongsoft.finance.domain.insight.PatternType;
import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.domain.transaction.Transaction;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import java.time.LocalDate;
import java.util.*;

public class AmountPattern implements Pattern {
  @Override
  public Optional<SpendingPattern> detect(
      Transaction transaction, List<EmbeddingMatch<TextSegment>> matches) {
    var amountPerDate =
        matches.stream()
            .map(
                match ->
                    new AbstractMap.SimpleEntry<>(
                        LocalDate.parse(
                            Objects.requireNonNull(match.embedded().metadata().getString("date"))),
                        match.embedded().metadata().getDouble("amount")))
            .sorted(Map.Entry.comparingByKey())
            .toList();

    var amountPatternType = detectPatternType(amountPerDate);
    if (amountPatternType != null) {
      var averageAmount = calculateAverage(amountPerDate);
      return Optional.of(
          SpendingPattern.builder()
              .type(amountPatternType)
              .category(transaction.getCategory())
              .detectedDate(transaction.getDate().withDayOfMonth(1))
              .confidence(0.85)
              .metadata(
                  Map.of(
                      "typical_amount", averageAmount,
                      "current_amount", transaction.computeAmount(transaction.computeFrom()),
                      "deviation_percent", calculateDeviationPercent(transaction, averageAmount)))
              .build());
    }

    return Optional.empty();
  }

  private PatternType detectPatternType(
      List<AbstractMap.SimpleEntry<LocalDate, Double>> amountPerDate) {
    var midPoint = amountPerDate.size() / 2;
    double firstHalfAvg =
        amountPerDate.subList(0, midPoint).stream()
            .mapToDouble(Map.Entry::getValue)
            .average()
            .orElse(0);

    double secondHalfAvg =
        amountPerDate.subList(midPoint, amountPerDate.size()).stream()
            .mapToDouble(Map.Entry::getValue)
            .average()
            .orElse(0);

    double percentChange = (secondHalfAvg - firstHalfAvg) / firstHalfAvg;

    if (percentChange > 0.15) {
      return PatternType.INCREASING_TREND;
    } else if (percentChange < -0.15) {
      return PatternType.DECREASING_TREND;
    }

    return null;
  }

  private double calculateAverage(List<AbstractMap.SimpleEntry<LocalDate, Double>> values) {
    return values.stream().mapToDouble(AbstractMap.SimpleEntry::getValue).average().orElse(0);
  }

  private double calculateDeviationPercent(Transaction transaction, double average) {
    double currentAmount = Math.abs(transaction.computeAmount(transaction.computeFrom()));
    return (currentAmount - average) / average * 100;
  }
}
