package com.jongsoft.finance.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.security.authentication.AuthorizationException;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

@Filter("/api/**")
public class ExceptionFilter extends OncePerRequestHttpServerFilter {

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        return Flowable.fromPublisher(chain.proceed(request))
                .onErrorReturn(throwable -> {
                    var error = new JsonError(throwable.getMessage());
                    error.link(Link.SELF, Link.of(request.getUri()));

                    int statusCode = 500;
                    if (throwable instanceof HttpStatusException e) {
                        statusCode = e.getStatus().getCode();
                    } else if (throwable instanceof AuthorizationException) {
                        statusCode = HttpStatus.UNAUTHORIZED.getCode();
                    }

                    return HttpResponse.
                            status(HttpStatus.valueOf(statusCode))
                            .body(error);
                });
    }

}
