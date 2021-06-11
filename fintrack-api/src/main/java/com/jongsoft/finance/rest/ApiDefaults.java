package com.jongsoft.finance.rest;

import io.micronaut.http.hateoas.JsonError;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD})
@ApiResponse(
        responseCode = "404",
        content = @Content(schema = @Schema(implementation = JsonError.class)),
        description = "Resource not found")
@ApiResponse(
        responseCode = "500",
        content = @Content(schema = @Schema(implementation = JsonError.class)),
        description = "Internal server error")
@ApiResponse(
        responseCode = "401",
        content = @Content(schema = @Schema(implementation = JsonError.class)),
        description = "The user is not authenticated, login first.")
public @interface ApiDefaults {
}
