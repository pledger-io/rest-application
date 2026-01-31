package com.jongsoft.finance.core.adapter.rest.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;

import org.reactivestreams.Publisher;

import java.time.Instant;

@Filter("/v2/api/**")
public class RequestLoggingFilter implements HttpServerFilter {

    private final MeterRegistry meterRegistry;

    public RequestLoggingFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.METRICS.order();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(
            HttpRequest<?> request, ServerFilterChain chain) {
        var startTime = Instant.now();

        return Publishers.then(chain.proceed(request), response -> {
            var endTime = Instant.now();
            var duration = endTime.toEpochMilli() - startTime.toEpochMilli();

            Timer.builder("http.custom.requests")
                    .tag("method", request.getMethod().name())
                    .tag("status", String.valueOf(response.getStatus().getCode()))
                    .tag("path", request.getPath())
                    .register(meterRegistry)
                    .record(java.time.Duration.ofMillis(duration));
        });
    }
}
