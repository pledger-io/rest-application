package com.jongsoft.finance.invoice.domain.jpa.mapper;

import com.jongsoft.finance.invoice.domain.jpa.entity.InvoiceJpa;
import com.jongsoft.finance.invoice.domain.model.Invoice;
import com.jongsoft.finance.invoice.domain.model.InvoiceLine;
import com.jongsoft.finance.project.domain.model.ClientIdentifier;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public abstract class InvoiceMapper {

    private final InvoiceTemplateMapper templateMapper;
    private final InvoiceLineMapper lineMapper;

    protected InvoiceMapper(InvoiceTemplateMapper templateMapper, InvoiceLineMapper lineMapper) {
        this.templateMapper = templateMapper;
        this.lineMapper = lineMapper;
    }

    @Mapper.Mapping(to = "client", from = "#{this.mapClientIdentifier(entity)}")
    @Mapper.Mapping(to = "template", from = "#{this.mapTemplate(entity)}")
    @Mapper.Mapping(to = "lines", from = "#{this.mapLines(entity)}")
    public abstract Invoice toDomain(InvoiceJpa entity);

    public ClientIdentifier mapClientIdentifier(InvoiceJpa entity) {
        return new ClientIdentifier(entity.getClient().getId());
    }

    public com.jongsoft.finance.invoice.domain.model.InvoiceTemplate mapTemplate(
            InvoiceJpa entity) {
        return templateMapper.toDomain(entity.getTemplate());
    }

    public List<InvoiceLine> mapLines(InvoiceJpa entity) {
        return entity.getLines().stream().map(lineMapper::toDomain).collect(Collectors.toList());
    }
}
