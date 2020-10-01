package com.jongsoft.finance.rest.importer;

import io.micronaut.core.annotation.Introspected;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Introspected
@NoArgsConstructor
class ImportSearchRequest {

    @Min(0)
    private int page;

    public ImportSearchRequest(@Min(0) int page) {
        this.page = page;
    }

    public int getPage() {
        return Math.max(0, page - 1);
    }
}
