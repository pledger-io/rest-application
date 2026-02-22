package com.jongsoft.finance.core.domain.jpa.mapper;

import com.jongsoft.finance.core.domain.jpa.entity.CurrencyJpa;
import com.jongsoft.finance.core.domain.model.Currency;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface CurrencyMapper {

    @Mapper()
    Currency toDomain(CurrencyJpa currencyJpa);
}
