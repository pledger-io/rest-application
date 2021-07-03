package com.jongsoft.finance.filter;

import com.jongsoft.finance.reactive.ContextPropagation;
import com.jongsoft.finance.reactive.ReactiveThreadLocal;
import com.jongsoft.finance.security.AuthenticationFacadeImpl;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.management.endpoint.EndpointsFilter;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.filters.SecurityFilter;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;

import javax.inject.Inject;
import java.security.Principal;
import java.util.UUID;

@Slf4j
@Filter("/**")
@Replaces(EndpointsFilter.class)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthenticationFilter extends OncePerRequestHttpServerFilter {
    public static final CharSequence AUTHENTICATION = HttpAttributes.PRINCIPAL.toString();

    private final AuthenticationFacadeImpl authenticationFacade;

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
                .doOnComplete(ContextPropagation::unsetContext)
                .onErrorReturn(exception -> this.translateException(exception, request));
    }

    private MutableHttpResponse<?> translateException(Throwable throwable, HttpRequest<?> request) {
        var error = new JsonError(throwable.getMessage());
        //error.link(Link.SELF, Link.of(request.getUri()));

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
