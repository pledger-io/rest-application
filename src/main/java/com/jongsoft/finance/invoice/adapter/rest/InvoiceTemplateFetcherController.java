package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.invoice.adapter.api.InvoiceTemplateProvider;
import com.jongsoft.finance.rest.InvoiceTemplateFetcherApi;
import com.jongsoft.finance.rest.model.InvoiceTemplateResponse;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
public class InvoiceTemplateFetcherController implements InvoiceTemplateFetcherApi {

    private final Logger logger;
    private final InvoiceTemplateProvider invoiceTemplateProvider;

    public InvoiceTemplateFetcherController(InvoiceTemplateProvider invoiceTemplateProvider) {
        this.invoiceTemplateProvider = invoiceTemplateProvider;
        this.logger = LoggerFactory.getLogger(InvoiceTemplateFetcherController.class);
    }

    @Override
    public List<InvoiceTemplateResponse> findInvoiceTemplates(String name) {
        logger.info("Fetching all invoice templates with provided filters.");

        if (name != null) {
            return invoiceTemplateProvider
                    .lookup(name)
                    .map(InvoiceTemplateMapper::toInvoiceTemplateResponse)
                    .map(List::of)
                    .getOrSupply(java.util.List::of);
        }

        // TODO: Implement search method in provider
        return List.of();
    }

    @Override
    public InvoiceTemplateResponse getInvoiceTemplateById(Long id) {
        logger.info("Fetching invoice template {}.", id);

        var template = invoiceTemplateProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Invoice template is not found"));

        return InvoiceTemplateMapper.toInvoiceTemplateResponse(template);
    }
}
