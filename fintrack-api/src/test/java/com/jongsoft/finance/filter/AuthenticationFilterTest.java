package com.jongsoft.finance.filter;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.security.AuthenticationFacadeImpl;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.security.filters.SecurityFilter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
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

        StepVerifier.create(subject.doFilter(mockRequest, Mockito.mock(ServerFilterChain.class)))
                .assertNext(step -> {
                    Assertions.assertThat(step.status().getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.getCode());
                })
                .verifyComplete();
    }

    @Test
    void doFilterOnce_ok() {
        var mockRequest = prepareMock();
        var chain = Mockito.mock(ServerFilterChain.class);

        Mockito.when(chain.proceed(mockRequest))
                .thenReturn(Publishers.just(HttpResponse.ok("OK")));

        StepVerifier.create(subject.doFilter(mockRequest, chain))
                .assertNext(step -> {
                    Assertions.assertThat(step.status().getCode()).isEqualTo(HttpStatus.OK.getCode());
                    Assertions.assertThat(step.getBody().get()).isEqualTo("OK");
                })
                .verifyComplete();
    }

    @Test
    void doFilterOnce_Exception() throws URISyntaxException {
        var mockRequest = prepareMock();
        var chain = Mockito.mock(ServerFilterChain.class);

        Mockito.doReturn(Publishers.just(StatusException.notFound("Cannot find exception")))
                .when(chain)
                .proceed(mockRequest);

        Mockito.when(mockRequest.getUri()).thenReturn(new URI("http://localhost"));

        StepVerifier.create(subject.doFilter(mockRequest, chain))
                .verifyErrorSatisfies(error -> {
                    Assertions.assertThat(error)
                            .isInstanceOf(StatusException.class)
                            .hasMessage("Cannot find exception");
                });
    }

    HttpRequest prepareMock() {
        var mockRequest = Mockito.mock(HttpRequest.class);

        Mockito.doReturn(Mockito.mock(HttpHeaders.class))
                .when(mockRequest)
                .getHeaders();

        Mockito.doReturn("/api/transactions")
                .when(mockRequest)
                .getPath();

        return mockRequest;
    }

}