package com.jongsoft.finance.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

import java.util.Locale;

@Filter("/**")
public class LocaleHeaderFilter extends OncePerRequestHttpServerFilter {

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(final HttpRequest<?> request, final ServerFilterChain chain) {
        request.getHeaders().get("Accept-Language", String.class)
                .ifPresent(s -> request.setAttribute(RequestAttributes.LOCALIZATION, Locale.forLanguageTag(s)));

        return chain.proceed(request);
    }

}
