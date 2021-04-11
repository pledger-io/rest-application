package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.Exportable;
import io.reactivex.Maybe;

public interface CurrencyProvider extends DataProvider<Currency>, Exportable<Currency> {

    Maybe<Currency> lookup(String code);

    @Override
    default boolean supports(Class<Currency> supportingClass) {
        return Currency.class.equals(supportingClass);
    }
}
