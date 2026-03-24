package com.jongsoft.finance.invoice.adapter.api;

import com.jongsoft.finance.invoice.domain.model.Invoice;
import com.jongsoft.lang.control.Optional;

public interface InvoiceProvider {

    Optional<Invoice> lookup(long id);

    Optional<Invoice> lookup(String invoiceNumber);
}
