package com.jongsoft.finance.core.adapter.api;

import com.jongsoft.finance.core.domain.model.Currency;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface CurrencyProvider {

    Optional<Currency> lookup(String code);

    Sequence<Currency> lookup();
}
