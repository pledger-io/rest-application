package com.jongsoft.finance.rest.importer;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Serdeable.Deserializable
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
