package com.jongsoft.finance.exporter.domain.jpa.mapper;

import com.jongsoft.finance.exporter.domain.jpa.entity.ImportConfig;
import com.jongsoft.finance.exporter.domain.jpa.entity.ImportJpa;
import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.finance.exporter.domain.model.BatchImportConfig;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public abstract class BatchImportMapper {

    private final ImportConfigMapper importConfigMapper;

    protected BatchImportMapper(ImportConfigMapper importConfigMapper) {
        this.importConfigMapper = importConfigMapper;
    }

    @Mapper.Mapping(to = "config", from = "#{this.mapConfig(source.config)}")
    public abstract BatchImport toModel(ImportJpa source);

    public BatchImportConfig mapConfig(ImportConfig jpa) {
        return importConfigMapper.toModel(jpa);
    }
}
