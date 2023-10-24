package com.jongsoft.finance.rest.importer;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;

@Serdeable.Deserializable
class TransactionSearchRequest {

    @Min(0)
    private int page;

    public TransactionSearchRequest(int page) {
        this.page = page;
    }

    public int getPage() {
        return Math.max(0, page - 1);
    }
}
