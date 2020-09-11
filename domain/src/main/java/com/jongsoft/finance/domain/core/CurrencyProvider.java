package com.jongsoft.finance.domain.core;

import com.jongsoft.lang.control.Optional;

public interface CurrencyProvider extends DataProvider<Currency>, Exportable<Currency> {

    Optional<Currency> lookup(String code);

    @Override
    default boolean supports(Class<Currency> supportingClass) {
        return Currency.class.equals(supportingClass);
    }
}
