package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import java.time.YearMonth;

public interface SpendingPatternProvider extends Exportable<SpendingPattern> {

    interface FilterCommand {
        FilterCommand category(String value, boolean exact);

        FilterCommand yearMonth(YearMonth yearMonth);

        FilterCommand page(int page, int pageSize);
    }

    /**
     * Gets all spending patterns for the current user.
     *
     * @return a sequence of all spending patterns
     */
    Sequence<SpendingPattern> lookup();

    /**
     * Gets spending patterns for a specific category.
     *
     * @param category the category to filter by
     * @return an optional spending pattern
     */
    Optional<SpendingPattern> lookup(String category);

    /**
     * Gets spending patterns for a specific year and month.
     *
     * @param yearMonth the year and month to filter by
     * @return a sequence of spending patterns
     */
    Sequence<SpendingPattern> lookup(YearMonth yearMonth);

    /**
     * Gets spending patterns based on a filter.
     *
     * @param filter the filter to apply
     * @return a page of spending patterns
     */
    ResultPage<SpendingPattern> lookup(FilterCommand filter);

    @Override
    default boolean supports(Class<?> supportingClass) {
        return SpendingPattern.class.equals(supportingClass);
    }

    default String typeOf() {
        return "SPENDING_PATTERN";
    }
}
