package com.jongsoft.finance.invoice.domain.model;

import com.jongsoft.finance.invoice.domain.commands.CreateInvoiceTemplateCommand;
import com.jongsoft.finance.invoice.domain.commands.UpdateInvoiceTemplateCommand;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;

@Introspected
public class InvoiceTemplate implements Serializable {

    private Long id;
    private String name;
    private String headerContent;
    private String footerContent;
    private String logoToken;

    // Used by the Mapper strategy
    InvoiceTemplate(
            Long id, String name, String headerContent, String footerContent, String logoToken) {
        this.id = id;
        this.name = name;
        this.headerContent = headerContent;
        this.footerContent = footerContent;
        this.logoToken = logoToken;
    }

    private InvoiceTemplate(
            String name, String headerContent, String footerContent, String logoToken) {
        this.name = name;
        this.headerContent = headerContent;
        this.footerContent = footerContent;
        this.logoToken = logoToken;
        CreateInvoiceTemplateCommand.templateCreated(name, headerContent, footerContent, logoToken);
    }

    public void update(String name, String headerContent, String footerContent, String logoToken) {
        this.name = name;
        this.headerContent = headerContent;
        this.footerContent = footerContent;
        this.logoToken = logoToken;
        UpdateInvoiceTemplateCommand.templateUpdated(
                id, name, headerContent, footerContent, logoToken);
    }

    public void delete() {
        com.jongsoft.finance.invoice.domain.commands.DeleteInvoiceTemplateCommand.templateDeleted(
                id);
    }

    public void attachLogo(String logoToken) {
        this.logoToken = logoToken;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHeaderContent() {
        return headerContent;
    }

    public String getFooterContent() {
        return footerContent;
    }

    public String getLogoToken() {
        return logoToken;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static InvoiceTemplate create(
            String name, String headerContent, String footerContent, String logoToken) {
        return new InvoiceTemplate(name, headerContent, footerContent, logoToken);
    }
}
