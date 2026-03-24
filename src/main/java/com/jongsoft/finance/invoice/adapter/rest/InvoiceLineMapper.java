package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.invoice.domain.model.InvoiceLine;
import com.jongsoft.finance.project.adapter.rest.TimeEntryMapper;
import com.jongsoft.finance.rest.model.InvoiceLineResponse;

import java.util.stream.Collectors;

interface InvoiceLineMapper {

    static InvoiceLineResponse toInvoiceLineResponse(InvoiceLine line) {
        var response = new InvoiceLineResponse(
                line.getId(),
                line.getDescription(),
                line.getQuantity().doubleValue(),
                line.getUnit(),
                line.getUnitPrice().doubleValue(),
                TaxBracketMapper.toTaxBracketResponse(line.getTaxBracket()));

        response.setTimeEntries(line.getTimeEntries().stream()
                .map(TimeEntryMapper::toTimeEntryResponse)
                .collect(Collectors.toList()));
        response.setSubtotal(line.getSubtotal().doubleValue());
        response.setTaxAmount(line.getTaxAmount().doubleValue());
        response.setTotal(line.getTotal().doubleValue());

        return response;
    }
}
