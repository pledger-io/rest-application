package com.jongsoft.finance.invoice.domain.jpa.mapper;

import com.jongsoft.finance.invoice.domain.jpa.entity.InvoiceLineJpa;
import com.jongsoft.finance.invoice.domain.model.InvoiceLine;
import com.jongsoft.finance.project.domain.jpa.mapper.TimeEntryMapper;
import com.jongsoft.finance.project.domain.model.TimeEntry;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public abstract class InvoiceLineMapper {

    private final TaxBracketMapper taxBracketMapper;
    private final TimeEntryMapper timeEntryMapper;

    protected InvoiceLineMapper(
            TaxBracketMapper taxBracketMapper, TimeEntryMapper timeEntryMapper) {
        this.taxBracketMapper = taxBracketMapper;
        this.timeEntryMapper = timeEntryMapper;
    }

    @Mapper.Mapping(to = "taxBracket", from = "#{this.mapTaxBracket(entity)}")
    @Mapper.Mapping(to = "timeEntries", from = "#{this.mapTimeEntries(entity)}")
    public abstract InvoiceLine toDomain(InvoiceLineJpa entity);

    public com.jongsoft.finance.invoice.domain.model.TaxBracket mapTaxBracket(
            InvoiceLineJpa entity) {
        return taxBracketMapper.toDomain(entity.getTaxBracket());
    }

    public List<TimeEntry> mapTimeEntries(InvoiceLineJpa entity) {
        return entity.getTimeEntries().stream()
                .map(timeEntryMapper::toDomain)
                .collect(Collectors.toList());
    }
}
