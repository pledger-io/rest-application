package com.jongsoft.finance.domain.core;

import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;

import java.util.function.Function;

public interface ResultPage<T> {

    default int pages() {
        return 0;
    }

    default long total() {
        return content().size();
    }

    default Sequence<T> content() {
        return API.List();
    }

    default boolean hasPages() {
        return pages() > 0;
    }

    default boolean hasNext() {
        return false;
    }

    <R> ResultPage<R> map(Function<T, R> mapper);

    static <T> ResultPage<T> empty() {
        return ResultPage.of();
    }

    static <T> ResultPage<T> of(T...elements) {
        return new ResultPage<T>() {
            @Override
            public Sequence<T> content() {
                return API.List(elements);
            }

            @Override
            public <R> ResultPage<R> map(Function<T, R> mapper) {
                return (ResultPage<R>) ResultPage.of(API.List(elements).iterator().toNativeArray());
            }
        };
    }
}
