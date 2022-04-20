package com.jongsoft.finance.filter;

import com.jongsoft.finance.security.AuthenticationFacadeImpl;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.filters.SecurityFilter;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Filter("/**")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthenticationFilter implements HttpServerFilter {
    public static final CharSequence AUTHENTICATION = HttpAttributes.PRINCIPAL.toString();

    private final AuthenticationFacadeImpl authenticationFacade;

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(final HttpRequest<?> request, final ServerFilterChain chain) {
        var securityFailure = request.getAttribute(SecurityFilter.REJECTION, HttpStatus.class);
        if (securityFailure.isPresent()) {
            return Publishers.just(
                    HttpResponse.status(securityFailure.get())
                        .body(new JsonError("Not authorized")));
        }

        var startTime = Instant.now();
        return Publishers.map(chain.proceed(request), response -> {
            if (request.getPath().contains("/api/localization/")) {
                log.trace("{}: {}", request.getMethod(), request.getPath());
            } else {
                log.debug("{}: {} in {} ms", request.getMethod(), request.getPath(), Duration.between(startTime, Instant.now()).toMillis());
            }
            return response;
        });
    }

    private MutableHttpResponse<?> translateException(Throwable throwable, HttpRequest<?> request) {
        var error = new JsonError(throwable.getMessage());

        int statusCode = 500;
        if (throwable instanceof AuthorizationException) {
            log.warn("{} - Attempt to access resource without proper authorization with message '{}'",
                    request.getPath(),
                    throwable.getMessage());
            statusCode = HttpStatus.UNAUTHORIZED.getCode();
        } else {
            var message = "%s - Exception caught in HTTP chain execution, with message '%s'".formatted(
                    request.getPath(),
                    throwable.getMessage());

            log.error(message, throwable);
        }

        return HttpResponse
                .status(HttpStatus.valueOf(statusCode))
                .body(error);
    }
}
