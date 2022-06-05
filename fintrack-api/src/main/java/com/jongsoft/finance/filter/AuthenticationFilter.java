package com.jongsoft.finance.filter;

import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.http.hateoas.JsonError;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Filter("/**")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthenticationFilter implements HttpServerFilter {
    public static final CharSequence AUTHENTICATION = HttpAttributes.PRINCIPAL.toString();

    private final ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher;

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(final HttpRequest<?> request, final ServerFilterChain chain) {
        var principalOpt = request.getUserPrincipal();
        if (principalOpt.isPresent()) {
            handleAuthentication(principalOpt.get());
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

        return Publishers.just(
                HttpResponse.status(HttpStatus.UNAUTHORIZED)
                        .body(new JsonError("Not authorized")));
    }

    private void handleAuthentication(Principal principal) {
        log.debug("Authenticated user {}", principal.getName());
        eventPublisher.publishEvent(new InternalAuthenticationEvent(this, principal.getName()));
    }
}
