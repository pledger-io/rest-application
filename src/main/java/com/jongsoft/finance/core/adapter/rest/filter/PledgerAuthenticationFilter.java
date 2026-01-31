package com.jongsoft.finance.core.adapter.rest.filter;

import com.jongsoft.finance.core.domain.model.Application;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.lang.Collections;

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

@Filter("/v2/api/**")
public class PledgerAuthenticationFilter implements HttpServerFilter {

    private final Logger logger;

    public PledgerAuthenticationFilter() {
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
            Application.authenticateUser(userName);
        }

        return chain.proceed(request);
    }

    private String handleOathUserCreation(ServerAuthentication authentication) {
        var hasEmail = authentication.getAttributes().containsKey("email");
        if (hasEmail) {
            var userName = authentication.getAttributes().get("email").toString();
            UserAccount.create(
                    userName,
                    authentication.getName(),
                    Collections.List(authentication.getRoles()));
            return userName;
        }

        return authentication.getName();
    }
}
