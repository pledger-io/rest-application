package com.jongsoft.finance.exporter.domain.jpa.mapper;

import com.jongsoft.finance.exporter.domain.jpa.entity.ImportConfig;
import com.jongsoft.finance.exporter.domain.model.BatchImportConfig;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface ImportConfigMapper {

    @Mapper
    BatchImportConfig toModel(ImportConfig entity);
}
