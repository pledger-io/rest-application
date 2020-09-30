package com.jongsoft.finance.rest.category;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CategorySearchRequest {

    private int page;

    public int getPage() {
        return Math.max(page - 1, 0);
    }

}
