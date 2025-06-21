package com.jongsoft.finance.providers;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionProvider extends DataProvider<Transaction> {

  /**
   * This command class helps to search in the database for relevant transactions. Use the methods
   * provided to add filters to the query. Calling a method twice will override the previous
   * value.
   */
  interface FilterCommand {
    /**
     * A list of {@link EntityRef} with unique identifiers of accounts that should be filtered
     * for.
     *
     * @param value the list of identifiers
     * @return this instance
     */
    FilterCommand accounts(Sequence<EntityRef> value);

    /**
     * A list of {@link EntityRef} with unique identifiers of categories that should be filtered
     * for.
     *
     * @param value the list of identifiers
     * @return this instance
     */
    FilterCommand categories(Sequence<EntityRef> value);

    /**
     * A list of {@link EntityRef} with unique identifiers of contracts that should be filtered
     * for.
     *
     * @param value the list of identifiers
     * @return this instance
     */
    FilterCommand contracts(Sequence<EntityRef> value);

    /**
     * A list of {@link EntityRef} with unique identifiers of expenses that should be filtered
     * for.
     *
     * @param value the list of identifiers
     * @return this instance
     */
    FilterCommand expenses(Sequence<EntityRef> value);

    /**
     * Add a filter on the name of one of the accounts involved in the transaction. Depending on
     * the value of the {@code exact} flag this will be an exact match or a partial match.
     *
     * @param value the name
     * @param exact should the name match exactly or partially
     * @return this instance
     */
    FilterCommand name(String value, boolean exact);

    /**
     * Add a filter for the description in the transaction. You can choose if the match should
     * be exact or partial using the {@code exact} parameter.
     *
     * @param value the description
     * @param exact should the name match exactly or partially
     * @return this instance
     */
    FilterCommand description(String value, boolean exact);

    /**
     * Add a date range to the filter for the transactions.
     *
     * @param range the active date range
     * @return this instance
     */
    FilterCommand range(Range<LocalDate> range);

    FilterCommand importSlug(String value);

    FilterCommand currency(String currency);

    /**
     * Indicates if the result should include only income or expenses.
     *
     * @param onlyIncome true for income, false for expenses
     * @return this instance
     */
    FilterCommand onlyIncome(boolean onlyIncome);

    /**
     * Filter to only include transactions to all of your own accounts. This will exclude some
     * transactions from the search. This operation is mutually exclusive with {@link
     * #transfers()} ()} and {@link #accounts(Sequence)}.
     *
     * @return this instance
     */
    FilterCommand ownAccounts();

    /**
     * Only include transactions between accounts owned by the user. This operation is mutually
     * exclusive with {@link #ownAccounts()} and {@link #accounts(Sequence)}.
     *
     * @return this instance
     */
    FilterCommand transfers();

    /**
     * Set the page to retrieve, if a page is set greater then available the result will be a
     * blank {@link ResultPage}.
     *
     * @param value the page
     * @return this instance
     */
    FilterCommand page(int value, int pageSize);
  }

  /**
   * The daily summary is a statistical data container. It will allow the system to provide the
   * aggregation of all transactions that occurred on one single day.
   */
  interface DailySummary {
    /**
     * The date for which this summary is valid.
     *
     * @return the day
     */
    LocalDate day();

    /**
     * The actual aggregated value for the given date.
     *
     * @return the summary
     */
    double summary();
  }

  /**
   * Locate the first ever transaction made that meets the preset given using the {@link
   * FilterCommand}.
   *
   * @param filter the filter to be applied
   * @return the first found transaction, or an empty otherwise
   */
  Optional<Transaction> first(FilterCommand filter);

  ResultPage<Transaction> lookup(FilterCommand filter);

  Sequence<DailySummary> daily(FilterCommand filter);

  /**
   * Retrieve a list of {@link DailySummary} for the given date range. The list will contain all
   * days of the month and the aggregated value on the first of every month.
   *
   * @param filterCommand the filter to be applied
   * @return the list of daily summaries
   */
  Sequence<DailySummary> monthly(FilterCommand filterCommand);

  Optional<BigDecimal> balance(FilterCommand filter);

  Sequence<Transaction> similar(EntityRef from, EntityRef to, double amount, LocalDate date);

  default boolean supports(Class<?> supportingClass) {
    return Transaction.class.equals(supportingClass);
  }
}
