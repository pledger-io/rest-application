package com.jongsoft.finance.rest.importer;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
class TransactionSearchRequest {

    @Min(0)
    private int page;

    public int getPage() {
        return Math.max(0, page - 1);
    }
}
