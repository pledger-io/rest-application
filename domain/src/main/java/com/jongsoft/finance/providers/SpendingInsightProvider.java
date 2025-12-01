package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import java.time.YearMonth;

public interface SpendingInsightProvider extends Exportable<SpendingInsight> {

    interface FilterCommand {
        FilterCommand category(String value, boolean exact);

        FilterCommand yearMonth(YearMonth yearMonth);

        FilterCommand page(int page, int pageSize);
    }

    /**
     * Gets all spending insights for the current user.
     *
     * @return a sequence of all spending insights
     */
    Sequence<SpendingInsight> lookup();

    /**
     * Gets spending insights for a specific category.
     *
     * @param category the category to filter by
     * @return an optional spending insight
     */
    Optional<SpendingInsight> lookup(String category);

    /**
     * Gets spending insights for a specific year and month.
     *
     * @param yearMonth the year and month to filter by
     * @return a sequence of spending insights
     */
    Sequence<SpendingInsight> lookup(YearMonth yearMonth);

    /**
     * Gets spending insights based on a filter.
     *
     * @param filter the filter to apply
     * @return a page of spending insights
     */
    ResultPage<SpendingInsight> lookup(FilterCommand filter);

    @Override
    default boolean supports(Class<?> supportingClass) {
        return SpendingInsight.class.equals(supportingClass);
    }
}
