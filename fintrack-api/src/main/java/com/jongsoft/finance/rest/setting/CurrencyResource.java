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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
    public List<CurrencyResponse> available() {
        return currencyProvider.lookup()
                .map(CurrencyResponse::new)
                .toJava();
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
    public CurrencyResponse create(@Valid @Body CurrencyRequest request) {
        var existing = currencyProvider.lookup(request.getCode());
        if (existing.isPresent()) {
            throw StatusException.badRequest("Currency with code " + request.getCode() + " already exists");
        }

        return new CurrencyResponse(new Currency(
                request.getName(),
                request.getCode(),
                request.getSymbol()
        ));
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
    public CurrencyResponse get(@PathVariable String currencyCode) {
        return currencyProvider.lookup(currencyCode)
                .map(CurrencyResponse::new)
                .getOrThrow(() -> StatusException.notFound(NO_CURRENCY_WITH_CODE_MESSAGE + currencyCode));
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
    public CurrencyResponse update(@PathVariable String currencyCode, @Valid @Body CurrencyRequest request) {
        return currencyProvider.lookup(currencyCode)
                .map(currency -> {
                    currency.rename(request.getName(), request.getCode(), request.getSymbol());
                    return currency;
                })
                .map(CurrencyResponse::new)
                .getOrThrow(() -> StatusException.notFound(NO_CURRENCY_WITH_CODE_MESSAGE + currencyCode));
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
    public CurrencyResponse patch(@PathVariable String currencyCode, @Valid @Body CurrencyPatchRequest request) {
        return currencyProvider.lookup(currencyCode)
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
                .map(CurrencyResponse::new)
                .getOrThrow(() -> StatusException.notFound(NO_CURRENCY_WITH_CODE_MESSAGE + currencyCode));
    }

}
