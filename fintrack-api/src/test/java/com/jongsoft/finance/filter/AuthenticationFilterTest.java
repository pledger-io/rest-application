package com.jongsoft.finance.filter;

import com.jongsoft.finance.security.AuthenticationFacadeImpl;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.security.filters.SecurityFilter;
import io.reactivex.Single;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class AuthenticationFilterTest {

    private AuthenticationFilter subject;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuthenticationFacadeImpl authenticationFacade;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        subject = new AuthenticationFilter(authenticationFacade);
    }

    @Test
    void doFilterOnce_unauthorized() {
        var mockRequest = Mockito.mock(HttpRequest.class);

        Mockito.when(mockRequest.getAttribute(SecurityFilter.REJECTION, HttpStatus.class))
                .thenReturn(Optional.of(HttpStatus.UNAUTHORIZED));

        var response = Single.fromPublisher(subject.doFilterOnce(mockRequest, Mockito.mock(ServerFilterChain.class)))
                .blockingGet();

        Assertions.assertThat(response.status().getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void doFilterOnce_ok() {
        var mockRequest = Mockito.mock(HttpRequest.class);
        var chain = Mockito.mock(ServerFilterChain.class);

        Mockito.when(chain.proceed(mockRequest))
                .thenReturn(Publishers.just(HttpResponse.ok("OK")));

        var response = Single.fromPublisher(subject.doFilterOnce(mockRequest, chain))
                .blockingGet();

        Assertions.assertThat(response.status().getCode()).isEqualTo(HttpStatus.OK.getCode());
        Assertions.assertThat(response.getBody().get()).isEqualTo("OK");
    }

}