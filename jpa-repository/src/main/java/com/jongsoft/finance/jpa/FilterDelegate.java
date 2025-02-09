package com.jongsoft.finance.jpa;

import java.util.Map;

public interface FilterDelegate<T extends FilterDelegate<T>> {

    record Sort(String field, boolean ascending) {}

    /**
     * Generates the HQL query that belongs to the command supported by the delegate.
     *
     * @return string with HQL query, starting with the 'from' part
     */
    String generateHql();

    /**
     * Create the sorting for the query.
     *
     * @return the sort
     */
    Sort sort();

    int page();

    int pageSize();

    Map<String, ?> getParameters();

    /**
     * Append a filtering for user.
     *
     * @param username
     */
    T user(String username);

}