package com.jongsoft.finance.filter;

import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.serde.ObjectMapper;
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

    private final ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(final HttpRequest<?> request, final ServerFilterChain chain) {
        request.getUserPrincipal()
                .ifPresent(this::handleAuthentication);

        var startTime = Instant.now();
        return Publishers.then(chain.proceed(request), response -> {
            if (request.getPath().contains("/api/localization/")) {
                log.trace("{}: {}", request.getMethod(), request.getPath());
            } else {
                if (log.isTraceEnabled() && request.getBody().isPresent()) {
                    Object body = request.getBody().get();
                    log.trace("{}: {} in {} ms, with request body {}.",
                            request.getMethod(),
                            request.getPath(),
                            Duration.between(startTime, Instant.now()).toMillis(),
                            Control.Try(() -> objectMapper.writeValueAsString(body))
                                    .recover(Throwable::getMessage).get());
                } else {
                    log.debug("{}: {} in {} ms", request.getMethod(), request.getPath(), Duration.between(startTime, Instant.now()).toMillis());
                }
            }
        });
    }

    private void handleAuthentication(Principal principal) {
        log.debug("Authenticated user {}", principal.getName());
        eventPublisher.publishEvent(new InternalAuthenticationEvent(this, principal.getName()));
    }
}
