package com.jongsoft.finance.security.filter;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.messaging.InternalAuthenticationEvent;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.security.authentication.ServerAuthentication;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Filter("/**")
public class PledgerAuthenticationFilter implements HttpServerFilter {

    private final Logger logger;
    private final FinTrack application;
    private final ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher;

    public PledgerAuthenticationFilter(
            FinTrack application,
            ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher) {
        this.application = application;
        this.eventPublisher = eventPublisher;
        this.logger = LoggerFactory.getLogger(PledgerAuthenticationFilter.class);
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(
            HttpRequest<?> request, ServerFilterChain chain) {
        if (request.getUserPrincipal().isPresent()) {
            var principal = request.getUserPrincipal().get();
            var userName = principal.getName();
            if (principal instanceof ServerAuthentication authentication) {
                userName = handleOathUserCreation(authentication);
            }

            logger.debug("User {} authenticated using HttpRequest.", userName);
            eventPublisher.publishEvent(new InternalAuthenticationEvent(this, userName));
        }

        return chain.proceed(request);
    }

    private String handleOathUserCreation(ServerAuthentication authentication) {
        var hasEmail = authentication.getAttributes().containsKey("email");
        if (hasEmail) {
            var userName = authentication.getAttributes().get("email").toString();
            application.createOathUser(
                    userName, authentication.getName(), List.copyOf(authentication.getRoles()));
            return userName;
        }

        return authentication.getName();
    }
}
