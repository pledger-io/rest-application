package com.jongsoft.finance.invoice.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.invoice.domain.commands.AddInvoiceLineCommand;
import com.jongsoft.finance.invoice.domain.commands.CreateInvoiceCommand;
import com.jongsoft.finance.invoice.domain.commands.FinalizeInvoiceCommand;
import com.jongsoft.finance.invoice.domain.commands.UpdateInvoiceCommand;
import com.jongsoft.finance.project.domain.model.ClientIdentifier;
import com.jongsoft.finance.project.domain.model.TimeEntry;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Introspected
public class Invoice implements Serializable {

    private Long id;
    private String invoiceNumber;
    private ClientIdentifier client;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private InvoiceTemplate template;
    private List<InvoiceLine> lines;
    private boolean finalized;
    private String pdfToken;

    // Used by the Mapper strategy
    Invoice(
            Long id,
            String invoiceNumber,
            ClientIdentifier client,
            LocalDate invoiceDate,
            LocalDate dueDate,
            InvoiceTemplate template,
            List<InvoiceLine> lines,
            boolean finalized,
            String pdfToken) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.client = client;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.template = template;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        this.finalized = finalized;
        this.pdfToken = pdfToken;
    }

    private Invoice(
            String invoiceNumber,
            ClientIdentifier client,
            LocalDate invoiceDate,
            LocalDate dueDate,
            InvoiceTemplate template) {
        if (invoiceDate.isAfter(dueDate)) {
            throw new IllegalArgumentException("Invoice date cannot be after due date.");
        }

        this.invoiceNumber = invoiceNumber;
        this.client = client;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.template = template;
        this.lines = new ArrayList<>();
        this.finalized = false;
        CreateInvoiceCommand.invoiceCreated(
                client.id(), invoiceNumber, invoiceDate, dueDate, template.getId());
    }

    public void update(
            String invoiceNumber,
            ClientIdentifier client,
            LocalDate invoiceDate,
            LocalDate dueDate,
            InvoiceTemplate template) {
        if (finalized) {
            throw StatusException.badRequest(
                    "Cannot update finalized invoice.", "invoice.finalized");
        }

        if (invoiceDate.isAfter(dueDate)) {
            throw new IllegalArgumentException("Invoice date cannot be after due date.");
        }

        this.invoiceNumber = invoiceNumber;
        this.client = client;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.template = template;
        UpdateInvoiceCommand.invoiceUpdated(id, invoiceNumber, invoiceDate, dueDate);
    }

    public void addLine(
            String description,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            TaxBracket taxBracket) {
        addLine(InvoiceLine.create(description, quantity, unit, unitPrice, taxBracket));
    }

    public void addLineFromTimeEntries(
            List<TimeEntry> timeEntries, String unit, BigDecimal unitPrice, TaxBracket taxBracket) {
        addLine(InvoiceLine.createFromTimeEntries(timeEntries, unit, unitPrice, taxBracket));
    }

    public void addLine(InvoiceLine line) {
        if (finalized) {
            throw StatusException.badRequest(
                    "Cannot add lines to finalized invoice.", "invoice.finalized");
        }
        this.lines.add(line);

        List<Long> timeEntryIds = line.getTimeEntries().stream()
                .map(TimeEntry::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (timeEntryIds.isEmpty()) {
            AddInvoiceLineCommand.lineAdded(
                    id,
                    line.getDescription(),
                    line.getQuantity(),
                    line.getUnit(),
                    line.getUnitPrice(),
                    line.getTaxBracket().getId());
        } else {
            AddInvoiceLineCommand.lineAddedWithTimeEntries(
                    id,
                    line.getDescription(),
                    line.getQuantity(),
                    line.getUnit(),
                    line.getUnitPrice(),
                    line.getTaxBracket().getId(),
                    timeEntryIds);
        }
    }

    public void finalize(String pdfToken) {
        if (finalized) {
            throw StatusException.badRequest(
                    "Invoice is already finalized.", "invoice.already.finalized");
        }
        if (lines.isEmpty()) {
            throw StatusException.badRequest(
                    "Cannot finalize invoice without lines.", "invoice.no.lines");
        }

        this.finalized = true;
        this.pdfToken = pdfToken;

        // Mark all attached time entries as invoiced
        lines.stream()
                .flatMap(line -> line.getTimeEntries().stream())
                .forEach(timeEntry -> timeEntry.markInvoiced());

        FinalizeInvoiceCommand.invoiceFinalized(id, pdfToken);
    }

    public BigDecimal getSubtotal() {
        return lines.stream()
                .map(InvoiceLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTaxTotal() {
        return lines.stream()
                .map(InvoiceLine::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getTaxTotal());
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public ClientIdentifier getClient() {
        return client;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public InvoiceTemplate getTemplate() {
        return template;
    }

    public List<InvoiceLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public boolean isFinalized() {
        return finalized;
    }

    public String getPdfToken() {
        return pdfToken;
    }

    public List<TimeEntry> getAllTimeEntries() {
        return lines.stream()
                .flatMap(line -> line.getTimeEntries().stream())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return this.getInvoiceNumber();
    }

    public static Invoice create(
            String invoiceNumber,
            ClientIdentifier client,
            LocalDate invoiceDate,
            LocalDate dueDate,
            InvoiceTemplate template) {
        return new Invoice(invoiceNumber, client, invoiceDate, dueDate, template);
    }
}
