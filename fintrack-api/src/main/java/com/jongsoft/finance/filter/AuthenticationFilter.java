package com.jongsoft.finance.filter;

import com.jongsoft.finance.reactive.ContextPropagation;
import com.jongsoft.finance.reactive.ReactiveThreadLocal;
import com.jongsoft.finance.security.AuthenticationFacadeImpl;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.management.endpoint.EndpointsFilter;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.SecurityFilter;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.Principal;
import java.util.UUID;

@Slf4j
@Filter("/**")
@Replaces(EndpointsFilter.class)
public class AuthenticationFilter extends OncePerRequestHttpServerFilter {
    public static final CharSequence AUTHENTICATION = HttpAttributes.PRINCIPAL.toString();

    private final AuthenticationFacadeImpl authenticationFacade;

    @Inject
    public AuthenticationFilter(AuthenticationFacadeImpl authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(final HttpRequest<?> request, final ServerFilterChain chain) {
        var securityFailure = request.getAttribute(SecurityFilter.REJECTION, HttpStatus.class);
        if (securityFailure.isPresent()) {
            return Publishers.just(
                    HttpResponse.status(securityFailure.get())
                        .body(new JsonError("Not authorized")));
        }

        var mdcContext = createMDCContext(request);
        var securityContext = createSecurityContext(request);

        if (request.getPath().contains("/api/localization/")) {
            log.trace("{}: {}", request.getMethod(), request.getPath());
        } else {
            log.info("{}: {}", request.getMethod(), request.getPath());
        }

        ContextPropagation.configureContext(mdcContext, securityContext);
        return Flowable.fromPublisher(chain.proceed(request))
                .doOnComplete(ContextPropagation::unsetContext);
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
