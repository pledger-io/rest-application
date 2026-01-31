package com.jongsoft.finance.spending.domain.jpa.mapper;

import com.jongsoft.finance.spending.domain.jpa.entity.SpendingPatternJpa;
import com.jongsoft.finance.spending.domain.model.SpendingPattern;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface SpendingPatternMapper {

    @Mapper
    SpendingPattern toModel(SpendingPatternJpa entity);
}
