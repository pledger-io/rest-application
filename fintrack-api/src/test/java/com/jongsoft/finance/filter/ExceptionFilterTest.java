package com.jongsoft.finance.filter;

import com.jongsoft.finance.rest.NotFoundException;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.hateoas.JsonError;
import io.reactivex.Single;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;

class ExceptionFilterTest {

    private ExceptionFilter subject;

    @BeforeEach
    void setup() {
        subject = new ExceptionFilter();
    }

    @Test
    void doFilterOnce() throws URISyntaxException {
        var mockRequest = Mockito.mock(HttpRequest.class);
        var chain = Mockito.mock(ServerFilterChain.class);

        Mockito.when(chain.proceed(mockRequest))
                .thenReturn(Publishers.just(new NotFoundException("Cannot find exception")));
        Mockito.when(mockRequest.getUri()).thenReturn(new URI("http://localhost"));

        var response = Single.fromPublisher(subject.doFilterOnce(mockRequest, chain))
                .blockingGet();

        Assertions.assertThat(response.getStatus().getCode()).isEqualTo(404);
        Assertions.assertThat(response.getBody(JsonError.class).get().getMessage()).isEqualTo("Cannot find exception");
    }

}