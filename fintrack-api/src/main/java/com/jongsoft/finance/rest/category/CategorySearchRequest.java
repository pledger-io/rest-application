package com.jongsoft.finance.rest.category;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Introspected
@NoArgsConstructor
@AllArgsConstructor
public class CategorySearchRequest {

    private int page;

    public int getPage() {
        return Math.max(page - 1, 0);
    }

}
