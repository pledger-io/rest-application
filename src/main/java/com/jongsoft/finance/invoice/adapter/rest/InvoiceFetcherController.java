package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.invoice.adapter.api.InvoiceProvider;
import com.jongsoft.finance.invoice.annotations.InvoiceModuleEnabled;
import com.jongsoft.finance.invoice.domain.model.Invoice;
import com.jongsoft.finance.project.adapter.api.ClientProvider;
import com.jongsoft.finance.project.domain.model.Client;
import com.jongsoft.finance.rest.InvoiceFetcherApi;
import com.jongsoft.finance.rest.model.InvoiceResponse;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@InvoiceModuleEnabled
class InvoiceFetcherController implements InvoiceFetcherApi {

    private final Logger logger;
    private final InvoiceProvider invoiceProvider;
    private final ClientProvider clientProvider;

    public InvoiceFetcherController(
            InvoiceProvider invoiceProvider, ClientProvider clientProvider) {
        this.invoiceProvider = invoiceProvider;
        this.clientProvider = clientProvider;
        this.logger = LoggerFactory.getLogger(InvoiceFetcherController.class);
    }

    @Override
    public List<InvoiceResponse> findInvoices(
            String invoiceNumber, Long clientId, Boolean finalized) {
        logger.info("Fetching all invoices with provided filters.");

        if (invoiceNumber != null) {
            return invoiceProvider
                    .lookup(invoiceNumber)
                    .map(this::toResponseWithClient)
                    .map(List::of)
                    .getOrSupply(java.util.List::of);
        }

        // TODO: Implement search method in provider with filters
        return List.of();
    }

    @Override
    public InvoiceResponse getInvoiceById(Long id) {
        logger.info("Fetching invoice {}.", id);

        var invoice = invoiceProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Invoice is not found"));

        return toResponseWithClient(invoice);
    }

    private InvoiceResponse toResponseWithClient(Invoice invoice) {
        Client client = clientProvider
                .lookup(invoice.getClient().id())
                .getOrThrow(() -> StatusException.notFound("Client is not found"));
        return InvoiceMapper.toInvoiceResponse(invoice, client);
    }
}
