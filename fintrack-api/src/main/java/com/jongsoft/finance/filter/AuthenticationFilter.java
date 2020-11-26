package com.jongsoft.finance.filter;

import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;
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
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.SecurityFilter;
import org.reactivestreams.Publisher;

import javax.inject.Inject;

@Filter("/**")
@Replaces(EndpointsFilter.class)
public class AuthenticationFilter extends OncePerRequestHttpServerFilter {
    public static final CharSequence AUTHENTICATION = HttpAttributes.PRINCIPAL.toString();

    private final ApplicationEventPublisher eventPublisher;

    @Inject
    public AuthenticationFilter(final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(final HttpRequest<?> request, final ServerFilterChain chain) {
        request.getAttribute(AUTHENTICATION)
                .map(raw -> (Authentication) raw)
                .ifPresent(auth -> eventPublisher.publishEvent(
                        new InternalAuthenticationEvent(
                                auth,
                                auth.getName())
                ));

        var securityFailure = request.getAttribute(SecurityFilter.REJECTION, HttpStatus.class);
        if (securityFailure.isPresent()) {
            return Publishers.just(
                    HttpResponse.status(securityFailure.get())
                        .body(new JsonError("Not authorized")));
        }

        return chain.proceed(request);
    }

}
