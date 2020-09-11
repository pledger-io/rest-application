package com.jongsoft.finance.domain.core;

import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;

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

    static <T> ResultPage<T> empty() {
        return new ResultPage<T>() {};
    }

    static <T> ResultPage<T> of(T...elements) {
        return new ResultPage<T>() {
            @Override
            public Sequence<T> content() {
                return API.List(elements);
            }
        };
    }
}
