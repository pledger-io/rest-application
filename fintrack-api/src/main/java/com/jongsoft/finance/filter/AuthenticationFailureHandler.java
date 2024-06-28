package com.jongsoft.finance.filter;

import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.authentication.DefaultAuthorizationExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces
@Singleton
@Replaces(DefaultAuthorizationExceptionHandler.class)
public class AuthenticationFailureHandler implements ExceptionHandler<AuthorizationException, HttpResponse<JsonError>> {

    private final Logger log = LoggerFactory.getLogger(AuthenticationFailureHandler.class);

    @Override
    public HttpResponse<JsonError> handle(HttpRequest request, AuthorizationException exception) {
        if (exception.isForbidden()) {
            log.info("{}: {} - User {} does not have access based upon the roles {}.",
                    request.getMethod(),
                    request.getPath(),
                    exception.getAuthentication().getName(),
                    exception.getAuthentication().getRoles());

            return HttpResponse.status(HttpStatus.FORBIDDEN)
                    .body(new JsonError("User does not have access based upon the roles"));
        }

        log.info("{}: {} - User {} is not authenticated.",
                request.getMethod(),
                request.getPath(),
                Control.Option(exception.getAuthentication())
                        .map(Authentication::getName)
                        .getOrSupply(() -> "Unknown"));

        return HttpResponse.unauthorized();
    }
}
