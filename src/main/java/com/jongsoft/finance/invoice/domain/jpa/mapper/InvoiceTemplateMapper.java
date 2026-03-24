package com.jongsoft.finance.invoice.domain.jpa.mapper;

import com.jongsoft.finance.invoice.domain.jpa.entity.InvoiceTemplateJpa;
import com.jongsoft.finance.invoice.domain.model.InvoiceTemplate;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface InvoiceTemplateMapper {

    @Mapper
    InvoiceTemplate toDomain(InvoiceTemplateJpa entity);
}
