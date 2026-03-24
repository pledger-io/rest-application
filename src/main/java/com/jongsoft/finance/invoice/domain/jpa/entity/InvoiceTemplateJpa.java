package com.jongsoft.finance.invoice.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

@Entity
@Introspected
@Table(name = "invoice_template")
public class InvoiceTemplateJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String headerContent;

    @Column(columnDefinition = "TEXT")
    private String footerContent;

    private String logoToken;

    @ManyToOne
    private UserAccountJpa user;

    public InvoiceTemplateJpa() {}

    private InvoiceTemplateJpa(
            String name, String headerContent, String footerContent, UserAccountJpa user) {
        this.name = name;
        this.headerContent = headerContent;
        this.footerContent = footerContent;
        this.user = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeaderContent() {
        return headerContent;
    }

    public void setHeaderContent(String headerContent) {
        this.headerContent = headerContent;
    }

    public String getFooterContent() {
        return footerContent;
    }

    public void setFooterContent(String footerContent) {
        this.footerContent = footerContent;
    }

    public String getLogoToken() {
        return logoToken;
    }

    public void setLogoToken(String logoToken) {
        this.logoToken = logoToken;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public static InvoiceTemplateJpa of(
            String name, String headerContent, String footerContent, UserAccountJpa user) {
        return new InvoiceTemplateJpa(name, headerContent, footerContent, user);
    }
}
