package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.invoice.adapter.api.InvoiceProvider;
import com.jongsoft.finance.invoice.adapter.api.InvoiceTemplateProvider;
import com.jongsoft.finance.invoice.adapter.api.TaxBracketProvider;
import com.jongsoft.finance.invoice.domain.model.Invoice;
import com.jongsoft.finance.project.adapter.api.ClientProvider;
import com.jongsoft.finance.project.adapter.api.TimeEntryProvider;
import com.jongsoft.finance.project.domain.model.ClientIdentifier;
import com.jongsoft.finance.rest.InvoiceCommandApi;
import com.jongsoft.finance.rest.model.FinalizeInvoiceRequest;
import com.jongsoft.finance.rest.model.InvoiceLineRequest;
import com.jongsoft.finance.rest.model.InvoiceRequest;
import com.jongsoft.finance.rest.model.InvoiceResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@Controller
class InvoiceCommandController implements InvoiceCommandApi {

    private final Logger logger;
    private final InvoiceProvider invoiceProvider;
    private final InvoiceTemplateProvider invoiceTemplateProvider;
    private final ClientProvider clientProvider;
    private final TaxBracketProvider taxBracketProvider;
    private final TimeEntryProvider timeEntryProvider;

    public InvoiceCommandController(
            InvoiceProvider invoiceProvider,
            InvoiceTemplateProvider invoiceTemplateProvider,
            ClientProvider clientProvider,
            TaxBracketProvider taxBracketProvider,
            TimeEntryProvider timeEntryProvider) {
        this.invoiceProvider = invoiceProvider;
        this.invoiceTemplateProvider = invoiceTemplateProvider;
        this.clientProvider = clientProvider;
        this.taxBracketProvider = taxBracketProvider;
        this.timeEntryProvider = timeEntryProvider;
        this.logger = LoggerFactory.getLogger(InvoiceCommandController.class);
    }

    @Override
    public HttpResponse<InvoiceResponse> createInvoice(InvoiceRequest invoiceRequest) {
        logger.info("Creating invoice {}.", invoiceRequest.getInvoiceNumber());

        var client = clientProvider
                .lookup(invoiceRequest.getClientId())
                .getOrThrow(
                        () -> StatusException.badRequest("Client not found", "client.not.found"));

        var template = invoiceTemplateProvider
                .lookup(invoiceRequest.getTemplateId())
                .getOrThrow(() ->
                        StatusException.badRequest("Template not found", "template.not.found"));

        var clientRef = new ClientIdentifier(client.getId());
        Invoice.create(
                invoiceRequest.getInvoiceNumber(),
                clientRef,
                invoiceRequest.getInvoiceDate(),
                invoiceRequest.getDueDate(),
                template);

        var invoice = invoiceProvider
                .lookup(invoiceRequest.getInvoiceNumber())
                .getOrThrow(() -> StatusException.internalError("Failed to create invoice"));

        return HttpResponse.created(InvoiceMapper.toInvoiceResponse(invoice, client));
    }

    @Override
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest invoiceRequest) {
        logger.info("Updating invoice {}.", id);

        var invoice = locateByIdOrThrow(id);
        var client = clientProvider
                .lookup(invoiceRequest.getClientId())
                .getOrThrow(
                        () -> StatusException.badRequest("Client not found", "client.not.found"));

        var template = invoiceTemplateProvider
                .lookup(invoiceRequest.getTemplateId())
                .getOrThrow(() ->
                        StatusException.badRequest("Template not found", "template.not.found"));

        invoice.update(
                invoiceRequest.getInvoiceNumber(),
                new ClientIdentifier(client.getId()),
                invoiceRequest.getInvoiceDate(),
                invoiceRequest.getDueDate(),
                template);

        return InvoiceMapper.toInvoiceResponse(invoice, client);
    }

    @Override
    public HttpResponse<InvoiceResponse> addInvoiceLine(
            Long id, InvoiceLineRequest invoiceLineRequest) {
        logger.info("Adding line to invoice {}.", id);

        var invoice = locateByIdOrThrow(id);
        var taxBracket = taxBracketProvider
                .lookup(invoiceLineRequest.getTaxBracketId())
                .getOrThrow(() -> StatusException.badRequest(
                        "Tax bracket not found", "tax.bracket.not.found"));

        if (invoiceLineRequest.getTimeEntryIds() != null
                && !invoiceLineRequest.getTimeEntryIds().isEmpty()) {
            var timeEntries = invoiceLineRequest.getTimeEntryIds().stream()
                    .map(timeEntryId -> timeEntryProvider
                            .lookup(timeEntryId)
                            .getOrThrow(() -> StatusException.badRequest(
                                    "Time entry not found", "time.entry.not.found")))
                    .toList();

            invoice.addLineFromTimeEntries(
                    timeEntries,
                    invoiceLineRequest.getUnit(),
                    BigDecimal.valueOf(invoiceLineRequest.getUnitPrice()),
                    taxBracket);
        } else {
            invoice.addLine(
                    invoiceLineRequest.getDescription(),
                    BigDecimal.valueOf(invoiceLineRequest.getQuantity()),
                    invoiceLineRequest.getUnit(),
                    BigDecimal.valueOf(invoiceLineRequest.getUnitPrice()),
                    taxBracket);
        }

        var client = clientProvider
                .lookup(invoice.getClient().id())
                .getOrThrow(() -> StatusException.notFound("Client is not found"));
        return HttpResponse.created(InvoiceMapper.toInvoiceResponse(invoice, client));
    }

    @Override
    public InvoiceResponse finalizeInvoice(Long id, FinalizeInvoiceRequest finalizeInvoiceRequest) {
        logger.info("Finalizing invoice {}.", id);

        var invoice = locateByIdOrThrow(id);
        invoice.finalize(finalizeInvoiceRequest.getPdfToken());

        var client = clientProvider
                .lookup(invoice.getClient().id())
                .getOrThrow(() -> StatusException.notFound("Client is not found"));
        return InvoiceMapper.toInvoiceResponse(invoice, client);
    }

    private Invoice locateByIdOrThrow(Long id) {
        return invoiceProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Invoice is not found"));
    }
}
