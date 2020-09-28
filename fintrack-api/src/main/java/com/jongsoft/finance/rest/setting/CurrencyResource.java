package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.rest.model.CurrencyResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;

@Tag(name = "Application Settings")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/settings/currencies")
public class CurrencyResource {

    private final CurrencyProvider currencyProvider;

    public CurrencyResource(CurrencyProvider currencyProvider) {
        this.currencyProvider = currencyProvider;
    }

    @Get
    @Operation(
            summary = "List all",
            description = "List all available currencies in the system",
            operationId = "getAllCurrencies"
    )
    public Flowable<CurrencyResponse> available() {
        return Flowable.fromIterable(currencyProvider.lookup().map(CurrencyResponse::new));
    }

    @Put
    @Secured("ADMIN")
    @Status(HttpStatus.CREATED)
    @Operation(
            summary = "Create currency",
            description = "Add a new currency to the system",
            operationId = "createCurrency"
    )
    public Single<CurrencyResponse> create(@Valid @Body CurrencyRequest request) {
        return currencyProvider.lookup(request.getCode())
                .switchIfEmpty(Single.just(
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
    public Single<CurrencyResponse> get(@PathVariable String currencyCode) {
        return currencyProvider.lookup(currencyCode)
                .switchIfEmpty(Single.error(
                        StatusException.notFound("No currency with code " + currencyCode + " exists.")))
                .map(CurrencyResponse::new);
    }

    @Secured("ADMIN")
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
    public Single<CurrencyResponse> update(@PathVariable String currencyCode, @Valid @Body CurrencyRequest request) {
        return currencyProvider.lookup(currencyCode)
                .switchIfEmpty(Single.error(
                        StatusException.notFound("No currency with code " + currencyCode + " exists.")))
                .map(currency -> {
                    currency.rename(request.getName(), request.getCode(), request.getSymbol());
                    return currency;
                })
                .map(CurrencyResponse::new);
    }

    @Secured("ADMIN")
    @Patch("/{currencyCode}")
    @Operation(
            summary = "Patch currency",
            description = "Partially update an existing currency in the system",
            operationId = "patchCurrency",
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
    public Single<CurrencyResponse> patch(@PathVariable String currencyCode, @Valid @Body CurrencyPatchRequest request) {
        return currencyProvider.lookup(currencyCode)
                .switchIfEmpty(Single.error(
                        StatusException.notFound("No currency with code " + currencyCode + " exists.")))
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
