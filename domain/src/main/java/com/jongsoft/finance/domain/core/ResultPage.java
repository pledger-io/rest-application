package com.jongsoft.finance.domain.core;

import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;

import java.util.function.Function;

public interface ResultPage<T> {

    default int pageSize() {
        return 20;
    }

    default int pages() {
        return 0;
    }

    default long total() {
        return content().size();
    }

    default Sequence<T> content() {
        return Collections.List();
    }

    default boolean hasPages() {
        return pages() > 0;
    }

    default boolean hasNext() {
        return false;
    }

    <R> ResultPage<R> map(Function<T, R> mapper);

    @SuppressWarnings("unchecked")
    static <T> ResultPage<T> empty() {
        return ResultPage.of();
    }

    static <T> ResultPage<T> of(T...elements) {
        return new ResultPage<T>() {
            @Override
            public Sequence<T> content() {
                return Collections.List(elements);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <R> ResultPage<R> map(Function<T, R> mapper) {
                return (ResultPage<R>) ResultPage.of(Collections.List(elements).map(mapper).iterator().toNativeArray());
            }
        };
    }
}
