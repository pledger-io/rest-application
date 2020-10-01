package com.jongsoft.finance.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;

import java.util.UUID;

@Filter("/api/**")
public class CorrelationIdFilter extends OncePerRequestHttpServerFilter {

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        var correlationId = request.getHeaders().get("X-Correlation-Id", String.class)
                .orElseGet(() -> UUID.randomUUID().toString());

        MDC.put("correlationId", correlationId);
        return Flowable.fromPublisher(chain.proceed(request))
                .doOnComplete(() -> {
                    MDC.remove("correlationId");
                });
    }

}
