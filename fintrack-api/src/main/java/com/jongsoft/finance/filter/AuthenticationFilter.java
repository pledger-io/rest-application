package com.jongsoft.finance.filter;

import com.jongsoft.finance.reactive.ContextPropagation;
import com.jongsoft.finance.reactive.ReactiveThreadLocal;
import com.jongsoft.finance.security.AuthenticationFacadeImpl;
import com.jongsoft.lang.Control;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.filters.SecurityFilter;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

        var mdcContext = createMDCContext(request);
        var securityContext = createSecurityContext(request);

        ContextPropagation.configureContext(mdcContext, securityContext);

        var startTime = Instant.now();
        return Publishers.map(chain.proceed(request), response -> {
            ContextPropagation.unsetContext();
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

    private ReactiveThreadLocal<?> createSecurityContext(final HttpRequest<?> request) {
        var username = request.getAttribute(AUTHENTICATION)
                .map(raw -> (Authentication) raw)
                .map(Principal::getName)
                .orElse(null);

        authenticationFacade.authenticate(username);
        return ReactiveThreadLocal.from(
                authenticationFacade::authenticated,
                authenticationFacade::authenticate);
    }

    private ReactiveThreadLocal<?> createMDCContext(final HttpRequest<?> request) {
        var correlationId = request.getHeaders().get("X-Correlation-Id", String.class)
                .orElseGet(() -> UUID.randomUUID().toString());

        MDC.put("correlationId", correlationId);
        return ReactiveThreadLocal.from(
                MDC::getCopyOfContextMap,
                contextMap -> Control.Option(contextMap)
                        .ifPresent(MDC::setContextMap));
    }

}
