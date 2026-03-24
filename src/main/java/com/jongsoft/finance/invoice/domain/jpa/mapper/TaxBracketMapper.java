package com.jongsoft.finance.invoice.domain.jpa.mapper;

import com.jongsoft.finance.invoice.domain.jpa.entity.TaxBracketJpa;
import com.jongsoft.finance.invoice.domain.model.TaxBracket;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface TaxBracketMapper {

    @Mapper
    TaxBracket toDomain(TaxBracketJpa entity);
}
