package com.jongsoft.finance.invoice.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.invoice.domain.commands.AttachTimeEntryToLineCommand;
import com.jongsoft.finance.project.domain.model.TimeEntry;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Introspected
public class InvoiceLine implements Serializable {

    private Long id;
    private String description;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private TaxBracket taxBracket;
    private List<TimeEntry> timeEntries;

    // Used by the Mapper strategy
    InvoiceLine(
            Long id,
            String description,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            TaxBracket taxBracket,
            List<TimeEntry> timeEntries) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.taxBracket = taxBracket;
        this.timeEntries = timeEntries != null ? new ArrayList<>(timeEntries) : new ArrayList<>();
    }

    private InvoiceLine(
            String description,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            TaxBracket taxBracket) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative.");
        }

        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.taxBracket = taxBracket;
        this.timeEntries = new ArrayList<>();
    }

    public BigDecimal getSubtotal() {
        return quantity.multiply(unitPrice);
    }

    public BigDecimal getTaxAmount() {
        return getSubtotal().multiply(taxBracket.getRate());
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getTaxAmount());
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public TaxBracket getTaxBracket() {
        return taxBracket;
    }

    public List<TimeEntry> getTimeEntries() {
        return Collections.unmodifiableList(timeEntries);
    }

    public void attachTimeEntry(TimeEntry timeEntry) {
        if (!timeEntry.getProject().isBillable()) {
            throw StatusException.badRequest(
                    "Time entries from non-billable projects cannot be invoiced.",
                    "project.not.billable");
        }
        this.timeEntries.add(timeEntry);
        if (this.id != null && timeEntry.getId() != null) {
            AttachTimeEntryToLineCommand.timeEntryAttached(this.id, timeEntry.getId());
        }
    }

    public static InvoiceLine create(
            String description,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            TaxBracket taxBracket) {
        return new InvoiceLine(description, quantity, unit, unitPrice, taxBracket);
    }

    public static InvoiceLine createFromTimeEntries(
            List<TimeEntry> timeEntries, String unit, BigDecimal unitPrice, TaxBracket taxBracket) {
        if (timeEntries == null || timeEntries.isEmpty()) {
            throw new IllegalArgumentException("Time entries list cannot be empty.");
        }

        BigDecimal totalHours = timeEntries.stream()
                .map(TimeEntry::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String description = timeEntries.size() == 1
                ? timeEntries.get(0).getDescription()
                : String.format("%d time entries", timeEntries.size());

        InvoiceLine line = new InvoiceLine(description, totalHours, unit, unitPrice, taxBracket);
        timeEntries.forEach(line::attachTimeEntry);
        return line;
    }
}
