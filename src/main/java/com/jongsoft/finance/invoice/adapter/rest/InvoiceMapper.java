package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.invoice.domain.model.Invoice;
import com.jongsoft.finance.project.adapter.rest.ClientMapper;
import com.jongsoft.finance.project.domain.model.Client;
import com.jongsoft.finance.rest.model.InvoiceResponse;

import java.util.stream.Collectors;

interface InvoiceMapper {

    static InvoiceResponse toInvoiceResponse(Invoice invoice, Client client) {
        var response = new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                ClientMapper.toClientResponse(client),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                InvoiceTemplateMapper.toInvoiceTemplateResponse(invoice.getTemplate()));

        response.setLines(invoice.getLines().stream()
                .map(InvoiceLineMapper::toInvoiceLineResponse)
                .collect(Collectors.toList()));
        response.setFinalized(invoice.isFinalized());
        response.setPdfToken(invoice.getPdfToken());
        response.setSubtotal(invoice.getSubtotal().doubleValue());
        response.setTaxTotal(invoice.getTaxTotal().doubleValue());
        response.setTotal(invoice.getTotal().doubleValue());

        return response;
    }
}
