package com.jongsoft.finance.http.filter;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@Filter("/v2/api/**")
public class RequestLoggingFilter implements HttpServerFilter {

    private final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public int getOrder() {
        return ServerFilterPhase.FIRST.before();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(
            HttpRequest<?> request, ServerFilterChain chain) {
        var startTime = Instant.now();

        return Publishers.then(chain.proceed(request), response -> {
            var endTime = Instant.now();
            var duration = endTime.toEpochMilli() - startTime.toEpochMilli();

            logger.info(
                    "{}: {} - {} ms - Status Code {}.",
                    request.getMethod(),
                    request.getPath(),
                    duration,
                    response.status());
        });
    }
}
