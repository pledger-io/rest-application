package com.jongsoft.finance.filter;

import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.security.authentication.AuthorizationException;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

@Slf4j
//@Filter("/api/**")
public class ExceptionFilter extends OncePerRequestHttpServerFilter {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        return Flowable.fromPublisher(chain.proceed(request))
                .onErrorReturn(throwable -> {
                    var error = new JsonError(throwable.getMessage());
                    error.link(Link.SELF, Link.of(request.getUri()));

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
                });
    }

}
