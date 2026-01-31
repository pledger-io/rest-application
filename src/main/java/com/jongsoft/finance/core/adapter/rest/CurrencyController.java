package com.jongsoft.finance.core.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.adapter.api.CurrencyProvider;
import com.jongsoft.finance.core.domain.model.Currency;
import com.jongsoft.finance.rest.CurrencyApi;
import com.jongsoft.finance.rest.model.CurrencyPatchRequest;
import com.jongsoft.finance.rest.model.CurrencyRequest;
import com.jongsoft.finance.rest.model.CurrencyResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
public class CurrencyController implements CurrencyApi {

    private final CurrencyProvider currencyProvider;
    private final Logger logger;

    public CurrencyController(CurrencyProvider currencyProvider) {
        this.currencyProvider = currencyProvider;
        this.logger = LoggerFactory.getLogger(CurrencyApi.class);
    }

    @Override
    public HttpResponse<@Valid CurrencyResponse> createCurrency(CurrencyRequest currencyRequest) {
        logger.info("Request to create new currency with code {}.", currencyRequest.getCode());
        if (currencyProvider.lookup(currencyRequest.getCode()).isPresent()) {
            throw StatusException.badRequest(
                    "Currency with code " + currencyRequest.getCode() + " already exists");
        }

        var currency = Currency.create(
                currencyRequest.getName(),
                currencyRequest.getCode(),
                currencyRequest.getSymbol().charAt(0));
        return HttpResponse.created(convert(currency));
    }

    @Override
    public List<@Valid CurrencyResponse> getCurrencies() {
        logger.info("Retrieving all currencies from the system.");

        return currencyProvider.lookup().map(this::convert).toJava();
    }

    @Override
    public CurrencyResponse getCurrencyByCode(String currencyCode) {
        logger.info("Retrieving currency by code {}.", currencyCode);

        return currencyProvider
                .lookup(currencyCode)
                .map(this::convert)
                .getOrThrow(() ->
                        StatusException.notFound("No currency found with code " + currencyCode));
    }

    @Override
    public CurrencyResponse patchCurrencyByCode(
            String currencyCode, CurrencyPatchRequest patchCurrencyRequest) {
        logger.info("Patching currency by code {}.", currencyCode);
        var currency = currencyProvider
                .lookup(currencyCode)
                .getOrThrow(() ->
                        StatusException.notFound("No currency found with code " + currencyCode));

        if (patchCurrencyRequest.getEnabled() != null) {
            if (patchCurrencyRequest.getEnabled().equals(true)) {
                currency.enable();
            } else {
                currency.disable();
            }
        }

        if (patchCurrencyRequest.getDecimalPlaces() != null) {
            currency.accuracy(patchCurrencyRequest.getDecimalPlaces());
        }

        return convert(currency);
    }

    @Override
    public CurrencyResponse updateCurrencyByCode(
            String currencyCode, CurrencyRequest currencyRequest) {
        logger.info("Updating currency by code {}.", currencyCode);
        return currencyProvider
                .lookup(currencyCode)
                .map(currency -> {
                    currency.rename(
                            currencyRequest.getName(),
                            currency.getCode(),
                            currencyRequest.getSymbol().charAt(0));
                    return currency;
                })
                .map(this::convert)
                .getOrThrow(() ->
                        StatusException.notFound("No currency found with code " + currencyCode));
    }

    private CurrencyResponse convert(Currency currency) {
        return new CurrencyResponse(
                currency.getName(),
                currency.getCode(),
                "" + currency.getSymbol(),
                currency.getDecimalPlaces(),
                currency.isEnabled());
    }
}
