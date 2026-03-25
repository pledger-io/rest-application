package com.jongsoft.finance.invoice.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jongsoft.finance.core.adapter.api.LocalizationCatalog;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.invoice.domain.model.Invoice;
import com.jongsoft.finance.invoice.domain.model.InvoiceLine;
import com.jongsoft.finance.invoice.domain.model.InvoiceTemplate;
import com.jongsoft.finance.invoice.domain.model.TaxBracket;
import com.jongsoft.finance.project.domain.model.Client;
import com.jongsoft.finance.project.domain.model.Project;
import com.jongsoft.finance.project.domain.model.TimeEntry;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Tag("unit")
@DisplayName("Unit - OpenPdf invoice PDF service")
class OpenPdfInvoicePdfServiceTest {

    private static final byte[] PDF_MAGIC = new byte[] {'%', 'P', 'D', 'F', '-'};

    private OpenPdfInvoicePdfService service;
    private Invoice invoice;
    private Client client;

    @BeforeEach
    void setUp() {
        StorageService storage = mock(StorageService.class);
        service = new OpenPdfInvoicePdfService(storage, new LocalizationCatalog() {

            @Override
            public String get(Locale locale, String key) {
                return key + "_" + locale.getLanguage();
            }

            @Override
            public Map<String, String> getCatalog(String language) {
                return Map.of();
            }
        });

        TaxBracket vat = mock(TaxBracket.class);
        when(vat.getRate()).thenReturn(new BigDecimal("0.21"));

        InvoiceLine line = mock(InvoiceLine.class);
        when(line.getDescription()).thenReturn("Consulting");
        when(line.getQuantity()).thenReturn(new BigDecimal("2"));
        when(line.getUnit()).thenReturn("hour");
        when(line.getUnitPrice()).thenReturn(new BigDecimal("50.00"));
        when(line.getTaxBracket()).thenReturn(vat);
        when(line.getTotal()).thenReturn(new BigDecimal("121.00"));
        when(line.getTimeEntries()).thenReturn(List.of());

        InvoiceTemplate template = mock(InvoiceTemplate.class);
        when(template.getLogoToken()).thenReturn(null);
        when(template.getHeaderContent()).thenReturn(null);
        when(template.getFooterContent()).thenReturn(null);

        invoice = mock(Invoice.class);
        when(invoice.getInvoiceNumber()).thenReturn("INV-UNIT-1");
        when(invoice.getInvoiceDate()).thenReturn(LocalDate.of(2026, 3, 1));
        when(invoice.getDueDate()).thenReturn(LocalDate.of(2026, 3, 31));
        when(invoice.getTemplate()).thenReturn(template);
        when(invoice.getLines()).thenReturn(List.of(line));
        when(invoice.getSubtotal()).thenReturn(new BigDecimal("100.00"));
        when(invoice.getTaxTotal()).thenReturn(new BigDecimal("21.00"));
        when(invoice.getTotal()).thenReturn(new BigDecimal("121.00"));

        client = mock(Client.class);
        when(client.getName()).thenReturn("Acme BV");
        when(client.getAddress()).thenReturn("1 Test Street");
        when(client.getEmail()).thenReturn("billing@acme.test");
        when(client.getPhone()).thenReturn("+31 20 000 0000");
    }

    @Test
    @DisplayName("stripSimpleMarkup removes tags and preserves line breaks")
    void stripSimpleMarkup_normalizesHtml() {
        String raw = "<p>Line one</p><br/>Line <b>two</b>";
        String out = OpenPdfInvoicePdfService.stripSimpleMarkup(raw);
        assertTrue(out.contains("Line one"));
        assertTrue(out.contains("Line two"));
    }

    @Test
    @DisplayName("Generated PDF has a valid header and non-trivial size")
    void renderPdf_producesPdfDocument() throws IOException {
        byte[] pdf = service.renderPdf(invoice, client, Locale.ENGLISH);
        assertTrue(pdf.length > 400, "expected non-trivial PDF size");
        assertArrayEquals(PDF_MAGIC, java.util.Arrays.copyOf(pdf, PDF_MAGIC.length));
    }

    @Test
    @DisplayName("German locale yields different bytes than English (bundles applied)")
    void renderPdf_localeAffectsOutput() {
        byte[] english = service.renderPdf(invoice, client, Locale.ENGLISH);
        byte[] german = service.renderPdf(invoice, client, Locale.GERMAN);
        assertFalse(Arrays.equals(english, german));
    }

    @Test
    @DisplayName("Extracted PDF text includes the invoice number")
    void renderPdf_includesInvoiceNumber() throws IOException {
        byte[] pdf = service.renderPdf(invoice, client, Locale.ENGLISH);
        String text = textFromPdf(pdf);
        assertTrue(text.contains("INV-UNIT-1"), "invoice number should appear in extracted text");
    }

    @Test
    @DisplayName("Dutch locale uses translated title in extracted text")
    void renderPdf_dutchContainsFactuur() throws IOException {
        byte[] pdf = service.renderPdf(invoice, client, Locale.forLanguageTag("nl"));
        String text = textFromPdf(pdf);

        assertThat(text)
                .describedAs("Dutch bundle should contain invoice.pdf.title_nl")
                .contains("invoice.pdf.title_nl");
    }

    @Test
    @DisplayName("Invoice without time entries is a single page")
    void renderPdf_withoutTimeEntries_singlePage() throws IOException {
        byte[] pdf = service.renderPdf(invoice, client, Locale.ENGLISH);
        try (PdfReader reader = new PdfReader(pdf)) {
            assertEquals(1, reader.getNumberOfPages());
        }
        String text = textFromPdf(pdf);
        assertThat(text).contains("invoice.pdf.invoiceNumber_en");
    }

    @Test
    @DisplayName("Invoice with time entries appends annex on a second page")
    void renderPdf_withTimeEntries_secondPageAnnex() throws IOException {
        Project project = mock(Project.class);
        when(project.getName()).thenReturn("Alpha");

        TimeEntry entry = mock(TimeEntry.class);
        when(entry.getDate()).thenReturn(LocalDate.of(2026, 2, 10));
        when(entry.getDescription()).thenReturn("Deep work on API");
        when(entry.getHours()).thenReturn(new BigDecimal("1.5"));
        when(entry.getProject()).thenReturn(project);

        InvoiceLine line = invoice.getLines().getFirst();
        when(line.getTimeEntries()).thenReturn(List.of(entry));

        byte[] pdf = service.renderPdf(invoice, client, Locale.ENGLISH);
        try (PdfReader reader = new PdfReader(pdf)) {
            assertTrue(reader.getNumberOfPages() >= 2, "annex should start on page 2");
        }
        String text = textFromPdf(pdf);

        assertThat(text).contains(" invoice.pdf.timeEntries.project").contains("Deep work on API");
    }

    private static String textFromPdf(byte[] pdf) throws IOException {
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder sb = new StringBuilder();
            for (int p = 1; p <= reader.getNumberOfPages(); p++) {
                sb.append(extractor.getTextFromPage(p));
            }
            return sb.toString();
        }
    }
}
