package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.invoice.domain.model.TaxBracket;
import com.jongsoft.finance.rest.model.TaxBracketResponse;

interface TaxBracketMapper {

    static TaxBracketResponse toTaxBracketResponse(TaxBracket taxBracket) {
        return new TaxBracketResponse(
                taxBracket.getId(), taxBracket.getName(), taxBracket.getRate().doubleValue());
    }
}
