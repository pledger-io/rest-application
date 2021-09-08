package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.ResultPage;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.util.List;

@Introspected
public class ResultPageResponse<T> {

    private final ResultPage<T> wrapped;

    public ResultPageResponse(ResultPage<T> wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Schema(description = "The actual contents of the page", required = true)
    public List<T> getContent() {
        return wrapped.content().toJava();
    }

    @Schema(description = "The meta-information for the page", required = true)
    public Info getInfo() {
        return new Info();
    }

    @Introspected
    public class Info {

        @Schema(description = "The total amount of matches", required = true, example = "20")
        public long getRecords() {
            return wrapped.total();
        }

        @Schema(description = "The amount of pages available", required = true, example = "2")
        public Integer getPages() {
            if (wrapped.hasPages()) {
                return wrapped.pages();
            }

            return null;
        }

        @Schema(description = "The amount of matches per page", required = true, example = "15")
        public Integer getPageSize() {
            if (wrapped.hasPages()) {
                return wrapped.pageSize();
            }

            return null;
        }

    }
}
