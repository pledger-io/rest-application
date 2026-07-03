package com.jongsoft.finance.spending.domain.service.detector.pattern;

import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.spending.domain.model.SpendingPattern;
import com.jongsoft.finance.spending.domain.service.detector.SpendingCategoryResolver;

import java.time.YearMonth;
import java.util.Optional;

/** Detects behavioral spending patterns for a category within an analyzed month. */
public interface Pattern {

    Optional<SpendingPattern> detect(
            String category, YearMonth forMonth, PatternMonthContext context);

    static String resolveCategory(Transaction transaction) {
        return SpendingCategoryResolver.resolve(transaction);
    }
}
