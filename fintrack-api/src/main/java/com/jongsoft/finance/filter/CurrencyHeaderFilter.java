package com.jongsoft.finance.filter;

import org.reactivestreams.Publisher;

import com.jongsoft.finance.domain.core.CurrencyProvider;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;

@Filter("/**")
public class CurrencyHeaderFilter extends OncePerRequestHttpServerFilter {

    private final CurrencyProvider currencyProvider;

    public CurrencyHeaderFilter(final CurrencyProvider currencyProvider) {
        this.currencyProvider = currencyProvider;
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(final HttpRequest<?> request, final ServerFilterChain chain) {
        request.getHeaders().get("X-Accept-Currency", String.class)
                .map(currencyProvider::lookup)
                .map(optional -> optional.getOrSupply(() -> null))
                .ifPresent(currency -> request.setAttribute(RequestAttributes.CURRENCY, currency));

        return chain.proceed(request);
    }

}
