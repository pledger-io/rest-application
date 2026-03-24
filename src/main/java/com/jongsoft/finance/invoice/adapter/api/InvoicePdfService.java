package com.jongsoft.finance.invoice.adapter.api;

import com.jongsoft.finance.invoice.domain.model.Invoice;
import com.jongsoft.finance.project.domain.model.Client;

import java.util.Locale;

public interface InvoicePdfService {

    /**
     * Renders a PDF using English labels and formats ({@link Locale#ENGLISH}).
     *
     * @see #renderPdf(Invoice, Client, Locale)
     */
    default byte[] renderPdf(Invoice invoice, Client client) {
        return renderPdf(invoice, client, Locale.ENGLISH);
    }

    /**
     * Renders a PDF for the given invoice and billing client. Labels come from {@code /i18n/messages}
     * and {@code /i18n/ValidationMessages} for the requested locale (supported: en, de, nl; others
     * fall back to English bundles). Dates and numbers follow the locale.
     *
     * <p>The invoice must contain line items; template header/footer and optional logo (via
     * {@link com.jongsoft.finance.invoice.domain.model.InvoiceTemplate#getLogoToken()}) are applied
     * when present.
     */
    byte[] renderPdf(Invoice invoice, Client client, Locale locale);
}
