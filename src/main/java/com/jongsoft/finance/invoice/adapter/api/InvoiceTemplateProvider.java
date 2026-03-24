package com.jongsoft.finance.invoice.adapter.api;

import com.jongsoft.finance.invoice.domain.model.InvoiceTemplate;
import com.jongsoft.lang.control.Optional;

public interface InvoiceTemplateProvider {

    Optional<InvoiceTemplate> lookup(long id);

    Optional<InvoiceTemplate> lookup(String name);
}
