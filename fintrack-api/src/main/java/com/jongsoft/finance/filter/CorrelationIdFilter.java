package com.jongsoft.finance.filter;

import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;

import java.util.UUID;

@Slf4j
@Filter("/api/**")
public class CorrelationIdFilter extends OncePerRequestHttpServerFilter {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        var correlationId = request.getHeaders().get("X-Correlation-Id", String.class)
                .orElseGet(() -> UUID.randomUUID().toString());

        MDC.put("correlationId", correlationId);
        log.trace("{}: {}", request.getMethod(), request.getPath());
        return Flowable.fromPublisher(chain.proceed(request))
                .doAfterTerminate(() -> {
                    MDC.remove("correlationId");
                });
    }

}
