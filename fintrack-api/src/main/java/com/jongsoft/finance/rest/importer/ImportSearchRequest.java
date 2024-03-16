package com.jongsoft.finance.rest.importer;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;

@Serdeable.Deserializable
record ImportSearchRequest(@Min(0) int page){

    public int getPage() {
        return Math.max(0, page - 1);
    }
}
