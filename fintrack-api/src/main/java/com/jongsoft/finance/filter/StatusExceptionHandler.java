package com.jongsoft.finance.filter;

import com.jongsoft.finance.core.exception.StatusException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces
@Singleton
public class StatusExceptionHandler implements ExceptionHandler<StatusException, HttpResponse<JsonError>> {

    private final Logger log = LoggerFactory.getLogger(StatusExceptionHandler.class);

    @Override
    public HttpResponse<JsonError> handle(HttpRequest request, StatusException exception) {
        if (exception.getStatusCode() != 404) {
            log.warn("{}: {} - Resource requested status {} with message: '{}'",
                    request.getMethod(),
                    request.getPath(),
                    exception.getStatusCode(),
                    exception.getMessage());
        } else {
            log.trace("{}: {} - Resource not found on server.", request.getMethod(), request.getPath());
        }

        var error = new JsonError(exception.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));

        if (exception.getLocalizationMessage() != null) {
            error.link(Link.HELP, exception.getLocalizationMessage());
        }

        return HttpResponse
                .status(HttpStatus.valueOf(exception.getStatusCode()))
                .body(error);
    }

}
