package com.jongsoft.finance.filter;

import com.jongsoft.finance.providers.CurrencyProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;

import javax.inject.Inject;

@Filter("/**")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CurrencyHeaderFilter extends OncePerRequestHttpServerFilter {

    private final CurrencyProvider currencyProvider;

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(final HttpRequest<?> request, final ServerFilterChain chain) {
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
