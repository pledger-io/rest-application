package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.invoice.adapter.api.InvoiceTemplateProvider;
import com.jongsoft.finance.invoice.domain.model.InvoiceTemplate;
import com.jongsoft.finance.rest.InvoiceTemplateCommandApi;
import com.jongsoft.finance.rest.model.InvoiceTemplateRequest;
import com.jongsoft.finance.rest.model.InvoiceTemplateResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class InvoiceTemplateCommandController implements InvoiceTemplateCommandApi {

    private final Logger logger;
    private final InvoiceTemplateProvider invoiceTemplateProvider;

    public InvoiceTemplateCommandController(InvoiceTemplateProvider invoiceTemplateProvider) {
        this.invoiceTemplateProvider = invoiceTemplateProvider;
        this.logger = LoggerFactory.getLogger(InvoiceTemplateCommandController.class);
    }

    @Override
    public HttpResponse<@Valid InvoiceTemplateResponse> createInvoiceTemplate(
            @io.micronaut.http.annotation.Body
                    @io.micronaut.core.annotation.Nullable(inherited = true)
                    @jakarta.validation.Valid
                    InvoiceTemplateRequest invoiceTemplateRequest) {
        logger.info("Creating invoice template {}.", invoiceTemplateRequest.getName());

        InvoiceTemplate.create(
                invoiceTemplateRequest.getName(),
                invoiceTemplateRequest.getHeaderContent(),
                invoiceTemplateRequest.getFooterContent(),
                invoiceTemplateRequest.getLogoToken());

        var template = invoiceTemplateProvider
                .lookup(invoiceTemplateRequest.getName())
                .getOrThrow(
                        () -> StatusException.internalError("Failed to create invoice template"));

        return HttpResponse.created(InvoiceTemplateMapper.toInvoiceTemplateResponse(template));
    }

    @Override
    public InvoiceTemplateResponse updateInvoiceTemplate(
            @io.micronaut.http.annotation.PathVariable("id") @jakarta.validation.constraints.NotNull
                    Long id,
            @io.micronaut.http.annotation.Body
                    @io.micronaut.core.annotation.Nullable(inherited = true)
                    @jakarta.validation.Valid
                    InvoiceTemplateRequest invoiceTemplateRequest) {
        logger.info("Updating invoice template {}.", id);

        var template = locateByIdOrThrow(id);
        template.update(
                invoiceTemplateRequest.getName(),
                invoiceTemplateRequest.getHeaderContent(),
                invoiceTemplateRequest.getFooterContent(),
                invoiceTemplateRequest.getLogoToken());

        return InvoiceTemplateMapper.toInvoiceTemplateResponse(template);
    }

    @Override
    public HttpResponse<Void> deleteInvoiceTemplateById(
            @io.micronaut.http.annotation.PathVariable("id") @jakarta.validation.constraints.NotNull
                    Long id) {
        logger.info("Deleting invoice template {}.", id);

        locateByIdOrThrow(id).delete();
        return HttpResponse.noContent();
    }

    private InvoiceTemplate locateByIdOrThrow(Long id) {
        return invoiceTemplateProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Invoice template is not found"));
    }
}
