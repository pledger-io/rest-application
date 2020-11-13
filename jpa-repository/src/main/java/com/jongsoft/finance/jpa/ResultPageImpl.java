package com.jongsoft.finance.jpa;

import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.lang.collection.Sequence;

import java.util.function.Function;

public class ResultPageImpl<T> implements ResultPage<T> {

    private final int limit;
    private final long totalRecords;
    private final Sequence<T> elements;

    public ResultPageImpl(Sequence<T> elements, int limit, long totalRecords) {
        this.elements = elements;
        this.limit = limit;
        this.totalRecords = totalRecords;
    }

    @Override
    public int pages() {
        if (limit < totalRecords) {
            return (int) (totalRecords / limit);
        } else {
            return 1;
        }
    }

    @Override
    public int pageSize() {
        return limit;
    }

    @Override
    public long total() {
        return totalRecords;
    }

    @Override
    public boolean hasNext() {
        if (pages() > 1) {
            return limit == elements.size();
        } else {
            return false;
        }
    }

    @Override
    public Sequence<T> content() {
        return elements;
    }

    @Override
    public <R> ResultPage<R> map(Function<T, R> mapper) {
        return new ResultPageImpl<>(
                elements.map(mapper),
                limit,
                totalRecords);
    }
}
