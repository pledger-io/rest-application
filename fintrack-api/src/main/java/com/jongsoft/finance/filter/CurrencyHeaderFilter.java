package com.jongsoft.finance.filter;

import com.jongsoft.finance.providers.CurrencyProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;

@Filter("/**")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CurrencyHeaderFilter implements HttpServerFilter {

    private final CurrencyProvider currencyProvider;

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(final HttpRequest<?> request, final ServerFilterChain chain) {
        var requestedCurrency = request.getHeaders().get("X-Accept-Currency", String.class);

        if (requestedCurrency.isPresent()) {
            currencyProvider.lookup(requestedCurrency.get())
                    .subscribe(currency -> request.setAttribute(RequestAttributes.CURRENCY, currency));
        } else {
            request.setAttribute(RequestAttributes.CURRENCY, "");
        }

        return chain.proceed(request);
    }

}
