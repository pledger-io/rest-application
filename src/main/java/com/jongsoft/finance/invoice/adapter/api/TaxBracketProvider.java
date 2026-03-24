package com.jongsoft.finance.invoice.adapter.api;

import com.jongsoft.finance.invoice.domain.model.TaxBracket;
import com.jongsoft.lang.control.Optional;

public interface TaxBracketProvider {

    Optional<TaxBracket> lookup(long id);

    Optional<TaxBracket> lookup(String name);
}
