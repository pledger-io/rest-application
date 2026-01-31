package com.jongsoft.finance.spending.domain.jpa.mapper;

import com.jongsoft.finance.spending.domain.jpa.entity.AnalyzeJobJpa;
import com.jongsoft.finance.spending.domain.model.AnalyzeJob;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface AnalyzeJobMapper {

    @Mapper.Mapping(to = "jobId", from = "id")
    @Mapper.Mapping(to = "month", from = "yearMonth")
    @Mapper.Mapping(to = "user", from = "#{entity.user.username}")
    AnalyzeJob toDomain(AnalyzeJobJpa entity);
}
