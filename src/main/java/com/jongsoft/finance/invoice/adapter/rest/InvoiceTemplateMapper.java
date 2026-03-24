package com.jongsoft.finance.invoice.adapter.rest;

import com.jongsoft.finance.invoice.domain.model.InvoiceTemplate;
import com.jongsoft.finance.rest.model.InvoiceTemplateResponse;

interface InvoiceTemplateMapper {

    static InvoiceTemplateResponse toInvoiceTemplateResponse(InvoiceTemplate template) {
        var response = new InvoiceTemplateResponse(template.getId(), template.getName());
        response.setHeaderContent(template.getHeaderContent());
        response.setFooterContent(template.getFooterContent());
        response.setLogoToken(template.getLogoToken());
        return response;
    }
}
