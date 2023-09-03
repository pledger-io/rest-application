package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.providers.CurrencyProvider;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.rest.model.CurrencyResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Tag(name = "Application Settings")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/settings/currencies")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CurrencyResource {

    private static final String NO_CURRENCY_WITH_CODE_MESSAGE = "No currency exists with code ";
    private final CurrencyProvider currencyProvider;

    @Get
    @Operation(
            summary = "List all",
            description = "List all available currencies in the system",
            operationId = "getAllCurrencies"
    )
    public Publisher<CurrencyResponse> available() {
        return Flux.fromIterable(currencyProvider.lookup().map(CurrencyResponse::new));
    }

    @Put
    @Secured("admin")
    @Status(HttpStatus.CREATED)
    @Operation(
            summary = "Create currency",
            description = "Add a new currency to the system",
            operationId = "createCurrency"
    )
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = CurrencyResponse.class)))
    public Publisher<CurrencyResponse> create(@Valid @Body CurrencyRequest request) {
        return currencyProvider.lookup(request.getCode())
                .switchIfEmpty(Mono.just(
                        new Currency(
                                request.getName(),
                                request.getCode(),
                                request.getSymbol())))
                .map(CurrencyResponse::new);
    }

    @Get("/{currencyCode}")
    @Operation(
            summary = "Get currency",
            description = "Returns an existing currency in the syste,",
            operationId = "getCurrency",
            responses = {
                    @ApiResponse(responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = CurrencyResponse.class)),
                            description = "The currency entity"),
                    @ApiResponse(responseCode = "404",
                            content = @Content(
                                    schema = @Schema(implementation = JsonError.class)),
                            description = "The exception that occurred")
            }
    )
    @ApiDefaults
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CurrencyResponse.class)), description = "The currency entity")
    public Publisher<CurrencyResponse> get(@PathVariable String currencyCode) {
        return currencyProvider.lookup(currencyCode)
                .switchIfEmpty(Mono.error(
                        StatusException.notFound(NO_CURRENCY_WITH_CODE_MESSAGE + currencyCode)))
                .map(CurrencyResponse::new);
    }

    @Secured("admin")
    @Post("/{currencyCode}")
    @Operation(
            summary = "Update currency",
            description = "Updates an existing currency in the system",
            operationId = "updateCurrency",
            responses = {
                    @ApiResponse(responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = CurrencyResponse.class)),
                            description = "The currency entity"),
                    @ApiResponse(responseCode = "404",
                            content = @Content(
                                    schema = @Schema(implementation = JsonError.class)),
                            description = "The exception that occurred")
            }
    )
    @ApiDefaults
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CurrencyResponse.class)), description = "The currency entity")
    public Publisher<CurrencyResponse> update(@PathVariable String currencyCode, @Valid @Body CurrencyRequest request) {
        return currencyProvider.lookup(currencyCode)
                .switchIfEmpty(Mono.error(
                        StatusException.notFound(NO_CURRENCY_WITH_CODE_MESSAGE + currencyCode)))
                .map(currency -> {
                    currency.rename(request.getName(), request.getCode(), request.getSymbol());
                    return currency;
                })
                .map(CurrencyResponse::new);
    }

    @Secured("admin")
    @Patch("/{currencyCode}")
    @Operation(
            summary = "Patch currency",
            description = "Partially update an existing currency in the system",
            operationId = "patchCurrency"
    )
    @ApiDefaults
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CurrencyResponse.class)), description = "The currency entity")
    public Publisher<CurrencyResponse> patch(@PathVariable String currencyCode, @Valid @Body CurrencyPatchRequest request) {
        return currencyProvider.lookup(currencyCode)
                .switchIfEmpty(Mono.error(
                        StatusException.notFound(NO_CURRENCY_WITH_CODE_MESSAGE + currencyCode)))
                .map(currency -> {
                    if (request.getEnabled() != null) {
                        if (request.getEnabled()) {
                            currency.enable();
                        } else {
                            currency.disable();
                        }
                    }

                    if (request.getDecimalPlaces() != null) {
                        currency.accuracy(request.getDecimalPlaces());
                    }

                    return currency;
                })
                .map(CurrencyResponse::new);
    }

}
