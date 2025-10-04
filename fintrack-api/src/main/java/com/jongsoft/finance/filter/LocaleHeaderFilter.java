package com.jongsoft.finance.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@Filter("/**")
public class LocaleHeaderFilter implements HttpServerFilter {

    private final Logger log = LoggerFactory.getLogger(LocaleHeaderFilter.class);

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(
            HttpRequest<?> request, ServerFilterChain chain) {
        log.debug("Filtering for locale {}", request.getHeaders().get("Accept-Language"));

        request.getHeaders()
                .get("Accept-Language", String.class)
                .ifPresent(s -> request.setAttribute(
                        RequestAttributes.LOCALIZATION, Locale.forLanguageTag(s)));

        return chain.proceed(request);
    }
}
