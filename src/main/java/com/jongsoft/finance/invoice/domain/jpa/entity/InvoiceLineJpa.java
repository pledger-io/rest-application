package com.jongsoft.finance.invoice.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;
import com.jongsoft.finance.project.domain.jpa.entity.TimeEntryJpa;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Introspected
@Table(name = "invoice_line")
public class InvoiceLineJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private InvoiceJpa invoice;

    private String description;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;

    @ManyToOne
    private TaxBracketJpa taxBracket;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "invoice_line_time_entry",
            joinColumns = @JoinColumn(name = "invoice_line_id"),
            inverseJoinColumns = @JoinColumn(name = "time_entry_id"))
    private List<TimeEntryJpa> timeEntries = new ArrayList<>();

    public InvoiceLineJpa() {}

    private InvoiceLineJpa(
            InvoiceJpa invoice,
            String description,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            TaxBracketJpa taxBracket) {
        this.invoice = invoice;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.taxBracket = taxBracket;
    }

    @Override
    public Long getId() {
        return id;
    }

    public InvoiceJpa getInvoice() {
        return invoice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public TaxBracketJpa getTaxBracket() {
        return taxBracket;
    }

    public void setTaxBracket(TaxBracketJpa taxBracket) {
        this.taxBracket = taxBracket;
    }

    public List<TimeEntryJpa> getTimeEntries() {
        return timeEntries;
    }

    public static InvoiceLineJpa of(
            InvoiceJpa invoice,
            String description,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            TaxBracketJpa taxBracket) {
        return new InvoiceLineJpa(invoice, description, quantity, unit, unitPrice, taxBracket);
    }
}
