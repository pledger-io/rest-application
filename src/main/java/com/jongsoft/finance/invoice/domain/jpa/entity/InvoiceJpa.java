package com.jongsoft.finance.invoice.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;
import com.jongsoft.finance.project.domain.jpa.entity.ClientJpa;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Introspected
@Table(name = "invoice")
public class InvoiceJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String invoiceNumber;

    @ManyToOne
    private ClientJpa client;

    private LocalDate invoiceDate;
    private LocalDate dueDate;

    @ManyToOne
    private InvoiceTemplateJpa template;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InvoiceLineJpa> lines = new ArrayList<>();

    private boolean finalized;
    private String pdfToken;

    @ManyToOne
    private UserAccountJpa user;

    public InvoiceJpa() {}

    private InvoiceJpa(
            String invoiceNumber,
            ClientJpa client,
            LocalDate invoiceDate,
            LocalDate dueDate,
            InvoiceTemplateJpa template,
            UserAccountJpa user) {
        this.invoiceNumber = invoiceNumber;
        this.client = client;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.template = template;
        this.finalized = false;
        this.user = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public ClientJpa getClient() {
        return client;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceTemplateJpa getTemplate() {
        return template;
    }

    public List<InvoiceLineJpa> getLines() {
        return lines;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public String getPdfToken() {
        return pdfToken;
    }

    public void setPdfToken(String pdfToken) {
        this.pdfToken = pdfToken;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public static InvoiceJpa of(
            String invoiceNumber,
            ClientJpa client,
            LocalDate invoiceDate,
            LocalDate dueDate,
            InvoiceTemplateJpa template,
            UserAccountJpa user) {
        return new InvoiceJpa(invoiceNumber, client, invoiceDate, dueDate, template, user);
    }
}
