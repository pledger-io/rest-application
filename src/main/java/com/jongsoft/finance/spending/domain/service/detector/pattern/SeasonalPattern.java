package com.jongsoft.finance.spending.domain.service.detector.pattern;

import com.jongsoft.finance.spending.domain.model.SpendingPattern;
import com.jongsoft.finance.spending.types.PatternType;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/** Detects when spending in a calendar month is elevated compared to the same month in prior years. */
public class SeasonalPattern implements Pattern {

    private static final double SEASONAL_MULTIPLIER = 2.0;
    private static final int MIN_PRIOR_YEARS = 2;

    @Override
    public Optional<SpendingPattern> detect(
            String category, YearMonth forMonth, PatternMonthContext context) {
        int calendarMonth = forMonth.getMonthValue();
        Map<Integer, Double> totalsByYear = new HashMap<>();

        for (EmbeddingMatch<TextSegment> match : context.historicMatches()) {
            LocalDate date = LocalDate.parse(
                    Objects.requireNonNull(match.embedded().metadata().getString("date")));
            if (date.getMonthValue() != calendarMonth) {
                continue;
            }
            YearMonth matchMonth = YearMonth.from(date);
            if (matchMonth.equals(forMonth)) {
                continue;
            }
            Double amount = match.embedded().metadata().getDouble("amount");
            if (amount != null) {
                totalsByYear.merge(date.getYear(), amount, Double::sum);
            }
        }

        if (totalsByYear.size() < MIN_PRIOR_YEARS) {
            return Optional.empty();
        }

        double currentMonthTotal = context.monthTransactions().stream()
                .mapToDouble(t -> t.computeAmount(t.computeTo()))
                .sum();
        double historicAvg = totalsByYear.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        if (historicAvg <= 0.0 || currentMonthTotal < historicAvg * SEASONAL_MULTIPLIER) {
            return Optional.empty();
        }

        var metadata = new HashMap<String, Object>();
        metadata.put("season", getSeason(forMonth));
        metadata.put("same_month_historic_avg", historicAvg);
        metadata.put("current_month_total", currentMonthTotal);
        metadata.put("years_observed", totalsByYear.size());
        metadata.put("lookback_months", context.lookbackMonths());

        return Optional.of(new SpendingPattern(
                PatternType.SEASONAL, category, 0.75, forMonth.atDay(1), metadata));
    }

    private String getSeason(YearMonth month) {
        int value = month.getMonthValue();
        if (value >= 3 && value <= 5) {
            return "Spring";
        } else if (value >= 6 && value <= 8) {
            return "Summer";
        } else if (value >= 9 && value <= 11) {
            return "Fall";
        }
        return "Winter";
    }
}
