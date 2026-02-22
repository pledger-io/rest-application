package com.jongsoft.finance.exporter.domain.jpa.mapper;

import com.jongsoft.finance.exporter.domain.jpa.entity.ImportJpa;
import com.jongsoft.finance.exporter.domain.model.BatchImport;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface BatchImportMapper {

    @Mapper
    BatchImport toModel(ImportJpa source);
}
