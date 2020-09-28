package com.jongsoft.finance.filter;

import com.jongsoft.finance.domain.core.CurrencyProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

@Filter("/**")
public class CurrencyHeaderFilter extends OncePerRequestHttpServerFilter {

    private final CurrencyProvider currencyProvider;

    public CurrencyHeaderFilter(final CurrencyProvider currencyProvider) {
        this.currencyProvider = currencyProvider;
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(final HttpRequest<?> request, final ServerFilterChain chain) {
        var requestedCurrency = request.getHeaders().get("X-Accept-Currency", String.class);
        if (requestedCurrency.isPresent()) {
            currencyProvider.lookup(requestedCurrency.get())
                    .subscribe(currency -> request.setAttribute(RequestAttributes.CURRENCY, currency));
        }

        return chain.proceed(request);
    }

}
