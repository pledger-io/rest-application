package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.domain.core.Currency;
import reactor.core.publisher.Mono;

public interface CurrencyProvider extends DataProvider<Currency>, Exportable<Currency> {

    Mono<Currency> lookup(String code);

    @Override
    default boolean supports(Class<Currency> supportingClass) {
        return Currency.class.equals(supportingClass);
    }
}
