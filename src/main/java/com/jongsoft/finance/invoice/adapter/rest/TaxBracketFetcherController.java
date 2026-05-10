package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.invoice.adapter.api.TaxBracketProvider;
import com.jongsoft.finance.invoice.annotations.InvoiceModuleEnabled;
import com.jongsoft.finance.rest.TaxBracketFetcherApi;
import com.jongsoft.finance.rest.model.TaxBracketResponse;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@InvoiceModuleEnabled
class TaxBracketFetcherController implements TaxBracketFetcherApi {

    private final Logger logger;
    private final TaxBracketProvider taxBracketProvider;

    public TaxBracketFetcherController(TaxBracketProvider taxBracketProvider) {
        this.taxBracketProvider = taxBracketProvider;
        this.logger = LoggerFactory.getLogger(TaxBracketFetcherController.class);
    }

    @Override
    public List<TaxBracketResponse> findTaxBrackets(String name) {
        logger.info("Fetching all tax brackets with provided filters.");

        if (name != null) {
            return taxBracketProvider
                    .lookup(name)
                    .map(TaxBracketMapper::toTaxBracketResponse)
                    .map(List::of)
                    .getOrSupply(java.util.List::of);
        }

        // TODO: Implement search method in provider
        return List.of();
    }

    @Override
    public TaxBracketResponse getTaxBracketById(Long id) {
        logger.info("Fetching tax bracket {}.", id);

        var taxBracket = taxBracketProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Tax bracket is not found"));

        return TaxBracketMapper.toTaxBracketResponse(taxBracket);
    }
}
