package com.jongsoft.finance.spending.domain.jpa.mapper;

import com.jongsoft.finance.spending.domain.jpa.entity.SpendingInsightJpa;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface SpendingInsightMapper {

    @Mapper
    SpendingInsight toModel(SpendingInsightJpa entity);
}
