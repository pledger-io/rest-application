package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.invoice.adapter.api.TaxBracketProvider;
import com.jongsoft.finance.invoice.domain.model.TaxBracket;
import com.jongsoft.finance.rest.TaxBracketCommandApi;
import com.jongsoft.finance.rest.model.TaxBracketRequest;
import com.jongsoft.finance.rest.model.TaxBracketResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class TaxBracketCommandController implements TaxBracketCommandApi {

    private final Logger logger;
    private final TaxBracketProvider taxBracketProvider;

    public TaxBracketCommandController(TaxBracketProvider taxBracketProvider) {
        this.taxBracketProvider = taxBracketProvider;
        this.logger = LoggerFactory.getLogger(TaxBracketCommandController.class);
    }

    @Override
    public HttpResponse<@Valid TaxBracketResponse> createTaxBracket(
            @io.micronaut.http.annotation.Body
                    @io.micronaut.core.annotation.Nullable(inherited = true)
                    @jakarta.validation.Valid
                    TaxBracketRequest taxBracketRequest) {
        logger.info("Creating tax bracket {}.", taxBracketRequest.getName());

        TaxBracket.create(
                taxBracketRequest.getName(),
                java.math.BigDecimal.valueOf(taxBracketRequest.getRate()));

        var taxBracket = taxBracketProvider
                .lookup(taxBracketRequest.getName())
                .getOrThrow(() -> StatusException.internalError("Failed to create tax bracket"));

        return HttpResponse.created(TaxBracketMapper.toTaxBracketResponse(taxBracket));
    }

    @Override
    public TaxBracketResponse updateTaxBracket(
            @io.micronaut.http.annotation.PathVariable("id") @jakarta.validation.constraints.NotNull
                    Long id,
            @io.micronaut.http.annotation.Body
                    @io.micronaut.core.annotation.Nullable(inherited = true)
                    @jakarta.validation.Valid
                    TaxBracketRequest taxBracketRequest) {
        logger.info("Updating tax bracket {}.", id);

        var taxBracket = locateByIdOrThrow(id);
        taxBracket.update(
                taxBracketRequest.getName(),
                java.math.BigDecimal.valueOf(taxBracketRequest.getRate()));

        return TaxBracketMapper.toTaxBracketResponse(taxBracket);
    }

    @Override
    public HttpResponse<Void> deleteTaxBracketById(
            @io.micronaut.http.annotation.PathVariable("id") @jakarta.validation.constraints.NotNull
                    Long id) {
        logger.info("Deleting tax bracket {}.", id);

        locateByIdOrThrow(id).delete();
        return HttpResponse.noContent();
    }

    private TaxBracket locateByIdOrThrow(Long id) {
        return taxBracketProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Tax bracket is not found"));
    }
}
