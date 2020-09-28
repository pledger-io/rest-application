package com.jongsoft.finance.domain.core;

import io.reactivex.Maybe;

public interface CurrencyProvider extends DataProvider<Currency>, Exportable<Currency> {

    Maybe<Currency> lookup(String code);

    @Override
    default boolean supports(Class<Currency> supportingClass) {
        return Currency.class.equals(supportingClass);
    }
}
