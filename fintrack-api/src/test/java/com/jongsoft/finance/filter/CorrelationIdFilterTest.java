package com.jongsoft.finance.filter;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.util.Optional;

class CorrelationIdFilterTest {

    private CorrelationIdFilter subject;

    @BeforeEach
    void setup() {
        subject = new CorrelationIdFilter();
    }

    @Test
    void doFilterOnce() {
        var mockRequest = Mockito.mock(HttpRequest.class);
        var headers = Mockito.mock(HttpHeaders.class);
        var chain = Mockito.mock(ServerFilterChain.class);

        Mockito.doReturn(headers).when(mockRequest).getHeaders();
        Mockito.doReturn(Optional.of("afd334d-fadf3-dfd3-dfd2s-dsf")).when(headers).get("X-Correlation-Id", String.class);
        Mockito.when(chain.proceed(mockRequest)).then(args -> {
            Assertions.assertThat(MDC.get("correlationId")).isEqualTo("afd334d-fadf3-dfd3-dfd2s-dsf");
            return Flowable.create(emitter -> {
                emitter.onComplete();
            }, BackpressureStrategy.LATEST);
        });

        TestSubscriber<MutableHttpResponse<?>> subscriber = new TestSubscriber<>();
        subject.doFilterOnce(mockRequest, chain).subscribe(subscriber);

        Assertions.assertThat(MDC.get("correlationId")).isNull();
    }

}