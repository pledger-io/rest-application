package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.core.ResultPage;

import java.util.List;

public class ResultPageResponse<T> {

    private final ResultPage<T> wrapped;

    public ResultPageResponse(ResultPage<T> wrapped) {
        this.wrapped = wrapped;
    }

    public List<T> getContent() {
        return wrapped.content().toJava();
    }

    public Info getInfo() {
        return new Info();
    }

    public class Info {

        public long getRecords() {
            return wrapped.total();
        }

        public Integer getPages() {
            if (wrapped.hasPages()) {
                return wrapped.pages();
            }

            return null;
        }

    }
}
