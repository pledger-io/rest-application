package com.jongsoft.finance;

import com.jongsoft.finance.core.exception.StatusException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Produces
@Singleton
public class StatusExceptionHandler
        implements ExceptionHandler<StatusException, HttpResponse<JsonError>> {

    @Override
    public HttpResponse<JsonError> handle(HttpRequest request, StatusException exception) {
        var error = new JsonError(exception.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));

        if (exception.getLocalizationMessage() != null) {
            error.link(Link.HELP, exception.getLocalizationMessage());
        }

        return HttpResponse.status(HttpStatus.valueOf(exception.getStatusCode())).body(error);
    }
}
