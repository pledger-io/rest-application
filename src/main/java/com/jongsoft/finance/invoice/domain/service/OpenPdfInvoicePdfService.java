package com.jongsoft.finance.invoice.domain.service;

import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.core.domain.service.LocalizableMessageCatalog;
import com.jongsoft.finance.invoice.adapter.api.InvoicePdfService;
import com.jongsoft.finance.invoice.domain.model.Invoice;
import com.jongsoft.finance.invoice.domain.model.InvoiceLine;
import com.jongsoft.finance.project.domain.model.Client;
import com.jongsoft.finance.project.domain.model.Project;
import com.jongsoft.finance.project.domain.model.TimeEntry;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.inject.Singleton;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Singleton
class OpenPdfInvoicePdfService implements InvoicePdfService {

    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color HEADER_TEXT = Color.WHITE;
    private static final Color TABLE_HEADER_BG = BRAND_BLUE;
    private static final Color TABLE_ROW_ALT_BG = new Color(245, 248, 255);
    private static final Color MUTED_TEXT = new Color(70, 70, 70);
    private static final Color PANEL_BG = new Color(250, 250, 252);

    private static final Font TITLE =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, BRAND_BLUE);
    private static final Font HEADING =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, MUTED_TEXT);
    private static final Font BODY =
            FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, MUTED_TEXT);
    private static final Font SMALL =
            FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, MUTED_TEXT);
    private static final Font TABLE_HEADER =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, HEADER_TEXT);
    private static final Font TOTAL_VALUE =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.BOLD, BRAND_BLUE);

    private static final String K_TITLE = "invoice.pdf.title";
    private static final String K_INVOICE_NUMBER = "invoice.pdf.invoiceNumber";
    private static final String K_INVOICE_DATE = "invoice.pdf.invoiceDate";
    private static final String K_DUE_DATE = "invoice.pdf.dueDate";
    private static final String K_BILL_TO = "invoice.pdf.billTo";
    private static final String K_COL_DESCRIPTION = "invoice.pdf.line.description";
    private static final String K_COL_QTY = "invoice.pdf.line.quantity";
    private static final String K_COL_UNIT = "invoice.pdf.line.unit";
    private static final String K_COL_UNIT_PRICE = "invoice.pdf.line.unitPrice";
    private static final String K_COL_TAX = "invoice.pdf.line.tax";
    private static final String K_COL_TOTAL = "invoice.pdf.line.total";
    private static final String K_TOTAL_SUBTOTAL = "invoice.pdf.total.subtotal";
    private static final String K_TOTAL_TAX = "invoice.pdf.total.tax";
    private static final String K_TOTAL_GRAND = "invoice.pdf.total.grandTotal";
    private static final String K_TIME_ENTRIES_TITLE = "invoice.pdf.timeEntries.title";
    private static final String K_TIME_ENTRIES_FOR_LINE = "invoice.pdf.timeEntries.forLine";
    private static final String K_TIME_ENTRIES_DATE = "invoice.pdf.timeEntries.date";
    private static final String K_TIME_ENTRIES_PROJECT = "invoice.pdf.timeEntries.project";
    private static final String K_TIME_ENTRIES_HOURS = "invoice.pdf.timeEntries.hours";
    private static final String K_TIME_ENTRIES_DESCRIPTION = "invoice.pdf.timeEntries.description";

    private final StorageService storageService;
    private final LocalizableMessageCatalog messages;

    OpenPdfInvoicePdfService(StorageService storageService, LocalizableMessageCatalog messages) {
        this.storageService = storageService;
        this.messages = messages;
    }

    @Override
    public byte[] renderPdf(Invoice invoice, Client client, Locale locale) {
        Locale loc = locale != null ? locale : Locale.ENGLISH;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, buffer);
            document.open();

            addOptionalLogo(document, invoice);
            addTemplateHeader(document, invoice);
            document.add(spacer(4f));

            document.add(new Paragraph(t(loc, K_TITLE), TITLE));
            document.add(spacer(8f));

            document.add(invoiceMetadataPanel(invoice, client, loc));
            document.add(spacer(10f));

            document.add(lineItemsTable(invoice, loc));
            document.add(spacer(6f));

            document.add(totalsTable(invoice, loc));
            document.add(spacer(12f));

            addTemplateFooter(document, invoice);

            if (invoiceHasTimeEntries(invoice)) {
                document.newPage();
                addTimeEntriesAnnex(document, invoice, loc);
            }

            document.close();
            return buffer.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate invoice PDF", e);
        }
    }

    private String t(Locale locale, String key) {
        return messages.get(locale, key);
    }

    private String lbl(Locale locale, String key, String value) {
        return messages.get(locale, key) + ": " + value;
    }

    private void addOptionalLogo(Document document, Invoice invoice) throws DocumentException {
        var token = invoice.getTemplate().getLogoToken();
        if (token == null || token.isBlank()) {
            return;
        }
        var logo = storageService.read(token);
        if (!logo.isPresent()) {
            return;
        }
        byte[] bytes = logo.get();
        try {
            Image img = Image.getInstance(bytes);
            img.scaleToFit(140, 70);
            img.setAlignment(Element.ALIGN_LEFT);
            document.add(img);
            document.add(new Paragraph(" ", SMALL));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read invoice logo image", e);
        }
    }

    private void addTemplateHeader(Document document, Invoice invoice) throws DocumentException {
        var header = invoice.getTemplate().getHeaderContent();
        if (header == null || header.isBlank()) {
            return;
        }
        for (String line : stripSimpleMarkup(header).split("\n")) {
            if (!line.isBlank()) {
                document.add(new Paragraph(line, BODY));
            }
        }
    }

    private void addTemplateFooter(Document document, Invoice invoice) throws DocumentException {
        var footer = invoice.getTemplate().getFooterContent();
        if (footer == null || footer.isBlank()) {
            return;
        }
        for (String line : stripSimpleMarkup(footer).split("\n")) {
            if (!line.isBlank()) {
                document.add(new Paragraph(line, SMALL));
            }
        }
    }

    private PdfPTable lineItemsTable(Invoice invoice, Locale locale) {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(2f);
        table.setWidths(new float[] {3.4f, 0.8f, 0.8f, 1.3f, 1f, 1.4f});

        addTableHeaderCell(table, t(locale, K_COL_DESCRIPTION), Element.ALIGN_LEFT);
        addTableHeaderCell(table, t(locale, K_COL_QTY), Element.ALIGN_RIGHT);
        addTableHeaderCell(table, t(locale, K_COL_UNIT), Element.ALIGN_LEFT);
        addTableHeaderCell(table, t(locale, K_COL_UNIT_PRICE), Element.ALIGN_RIGHT);
        addTableHeaderCell(table, t(locale, K_COL_TAX), Element.ALIGN_RIGHT);
        addTableHeaderCell(table, t(locale, K_COL_TOTAL), Element.ALIGN_RIGHT);

        int row = 0;
        for (InvoiceLine line : invoice.getLines()) {
            boolean alternate = row++ % 2 == 1;
            addBodyCell(table, line.getDescription(), Element.ALIGN_LEFT, alternate);
            addBodyCell(
                    table,
                    formatQuantity(locale, line.getQuantity()),
                    Element.ALIGN_RIGHT,
                    alternate);
            addBodyCell(table, line.getUnit(), Element.ALIGN_LEFT, alternate);
            addBodyCell(
                    table,
                    formatMoney(locale, line.getUnitPrice()),
                    Element.ALIGN_RIGHT,
                    alternate);
            addBodyCell(
                    table,
                    taxPercent(locale, line.getTaxBracket().getRate()),
                    Element.ALIGN_RIGHT,
                    alternate);
            addBodyCell(
                    table, formatMoney(locale, line.getTotal()), Element.ALIGN_RIGHT, alternate);
        }

        return table;
    }

    private static boolean invoiceHasTimeEntries(Invoice invoice) {
        for (InvoiceLine line : invoice.getLines()) {
            List<TimeEntry> entries = line.getTimeEntries();
            if (entries != null && !entries.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void addTimeEntriesAnnex(Document document, Invoice invoice, Locale locale)
            throws DocumentException {
        document.add(new Paragraph(t(locale, K_TIME_ENTRIES_TITLE), TITLE));
        document.add(spacer(8f));

        for (InvoiceLine line : invoice.getLines()) {
            List<TimeEntry> entries =
                    line.getTimeEntries() == null ? Collections.emptyList() : line.getTimeEntries();
            if (entries.isEmpty()) {
                continue;
            }

            String lineHeading = t(locale, K_TIME_ENTRIES_FOR_LINE) + ": " + line.getDescription();
            document.add(new Paragraph(lineHeading, HEADING));
            document.add(spacer(3f));

            document.add(timeEntriesTable(entries, locale));
            document.add(spacer(8f));
        }
    }

    private PdfPTable timeEntriesTable(List<TimeEntry> entries, Locale locale) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(2f);
        table.setSpacingAfter(2f);
        table.setWidths(new float[] {1.2f, 2.2f, 0.8f, 3.2f});

        addTableHeaderCell(table, t(locale, K_TIME_ENTRIES_DATE), Element.ALIGN_LEFT);
        addTableHeaderCell(table, t(locale, K_TIME_ENTRIES_PROJECT), Element.ALIGN_LEFT);
        addTableHeaderCell(table, t(locale, K_TIME_ENTRIES_HOURS), Element.ALIGN_RIGHT);
        addTableHeaderCell(table, t(locale, K_TIME_ENTRIES_DESCRIPTION), Element.ALIGN_LEFT);

        int row = 0;
        for (TimeEntry entry : entries) {
            boolean alternate = row++ % 2 == 1;
            addBodyCell(table, formatDate(locale, entry.getDate()), Element.ALIGN_LEFT, alternate);
            addBodyCell(table, projectLabel(entry.getProject()), Element.ALIGN_LEFT, alternate);
            addBodyCell(
                    table,
                    formatQuantity(locale, entry.getHours()),
                    Element.ALIGN_RIGHT,
                    alternate);
            String desc = entry.getDescription() == null ? "" : entry.getDescription();
            addBodyCell(table, desc, Element.ALIGN_LEFT, alternate);
        }

        return table;
    }

    private static String projectLabel(Project project) {
        if (project == null) {
            return "";
        }
        return project.getName() != null ? project.getName() : "";
    }

    private PdfPTable totalsTable(Invoice invoice, Locale locale) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(2f);

        addTotalRow(table, t(locale, K_TOTAL_SUBTOTAL), formatMoney(locale, invoice.getSubtotal()));
        addTotalRow(table, t(locale, K_TOTAL_TAX), formatMoney(locale, invoice.getTaxTotal()));
        addGrandTotalRow(table, t(locale, K_TOTAL_GRAND), formatMoney(locale, invoice.getTotal()));

        return table;
    }

    private void addTableHeaderCell(PdfPTable table, String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER));
        cell.setBackgroundColor(TABLE_HEADER_BG);
        cell.setBorderColor(new Color(210, 220, 240));
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, int align, boolean alternate) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY));
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(new Color(225, 230, 240));
        if (alternate) {
            cell.setBackgroundColor(TABLE_ROW_ALT_BG);
        }
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, HEADING));
        l.setBorder(PdfPCell.NO_BORDER);
        l.setPadding(4);
        table.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(value, HEADING));
        v.setBorder(PdfPCell.NO_BORDER);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setPadding(4);
        table.addCell(v);
    }

    private void addGrandTotalRow(PdfPTable table, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, HEADING));
        l.setPadding(6);
        l.setBackgroundColor(PANEL_BG);
        l.setBorderColor(new Color(210, 220, 240));
        table.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase(value, TOTAL_VALUE));
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setPadding(6);
        v.setBackgroundColor(PANEL_BG);
        v.setBorderColor(new Color(210, 220, 240));
        table.addCell(v);
    }

    private PdfPTable invoiceMetadataPanel(Invoice invoice, Client client, Locale locale) {
        PdfPTable panel = new PdfPTable(2);
        panel.setWidthPercentage(100);
        panel.setWidths(new float[] {2.8f, 1.2f});

        PdfPCell billTo = new PdfPCell();
        billTo.setPadding(8);
        billTo.setBackgroundColor(PANEL_BG);
        billTo.setBorderColor(new Color(225, 230, 240));
        billTo.addElement(new Paragraph(t(locale, K_BILL_TO), HEADING));
        billTo.addElement(new Paragraph(client.getName(), BODY));
        if (client.getAddress() != null && !client.getAddress().isBlank()) {
            billTo.addElement(new Paragraph(stripSimpleMarkup(client.getAddress()), BODY));
        }
        if (client.getEmail() != null && !client.getEmail().isBlank()) {
            billTo.addElement(new Paragraph(client.getEmail(), BODY));
        }
        if (client.getPhone() != null && !client.getPhone().isBlank()) {
            billTo.addElement(new Paragraph(client.getPhone(), BODY));
        }
        panel.addCell(billTo);

        PdfPCell meta = new PdfPCell();
        meta.setPadding(8);
        meta.setBackgroundColor(PANEL_BG);
        meta.setBorderColor(new Color(225, 230, 240));
        meta.addElement(
                new Paragraph(lbl(locale, K_INVOICE_NUMBER, invoice.getInvoiceNumber()), BODY));
        meta.addElement(new Paragraph(
                lbl(locale, K_INVOICE_DATE, formatDate(locale, invoice.getInvoiceDate())), BODY));
        meta.addElement(new Paragraph(
                lbl(locale, K_DUE_DATE, formatDate(locale, invoice.getDueDate())), BODY));
        panel.addCell(meta);

        return panel;
    }

    private static Paragraph spacer(float spacingAfter) {
        Paragraph paragraph = new Paragraph(" ", SMALL);
        paragraph.setSpacingAfter(spacingAfter);
        return paragraph;
    }

    private static String formatMoney(Locale locale, BigDecimal amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount);
    }

    private static String formatQuantity(Locale locale, BigDecimal quantity) {
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(4);
        nf.setRoundingMode(RoundingMode.HALF_UP);
        return nf.format(quantity);
    }

    private static String taxPercent(Locale locale, BigDecimal rate) {
        BigDecimal pct = rate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(2);
        return nf.format(pct) + "%";
    }

    private static String formatDate(Locale locale, LocalDate date) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale)
                .format(date);
    }

    /**
     * Turns common HTML-ish content into plain text for PDF; keeps line breaks from {@code <br>} and
     * {@code <p>}.
     */
    static String stripSimpleMarkup(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw;
        s = s.replaceAll("(?i)<\\s*br\\s*/?>", "\n");
        s = s.replaceAll("(?i)</\\s*p\\s*>", "\n");
        s = s.replaceAll("(?i)<\\s*p[^>]*>", "");
        s = s.replaceAll("<[^>]+>", " ");
        s = s.replaceAll("[ \t]+", " ");
        return s.trim();
    }
}
