package com.jongsoft.finance.rest.category;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Builder
@Serdeable.Deserializable
public class CategorySearchRequest {

    private int page;

    public int getPage() {
        return Math.max(page - 1, 0);
    }

}
