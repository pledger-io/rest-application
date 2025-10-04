package com.jongsoft.finance.http.filter;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;

import org.reactivestreams.Publisher;
import org.slf4j.MDC;

import java.util.UUID;

@Filter("/**")
public class CorrelationIdFilter implements HttpServerFilter {
    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(
            HttpRequest<?> request, ServerFilterChain chain) {
        if (request.getHeaders().contains("X-Correlation-Id")) {
            MDC.put("correlationId", request.getHeaders().get("X-Correlation-Id"));
        } else {
            MDC.put("correlationId", UUID.randomUUID().toString());
        }

        return Publishers.then(chain.proceed(request), response -> {
            MDC.remove("correlationId");
        });
    }
}
