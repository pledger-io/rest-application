package com.jongsoft.finance.exporter.domain.model;

import com.jongsoft.finance.exporter.domain.commands.CreateConfigurationCommand;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class BatchImportConfig {

    private Long id;

    private String name;
    private String fileCode;
    private String type;

    private BatchImportConfig(String type, String name, String fileCode) {
        this.name = name;
        this.fileCode = fileCode;
        this.type = type;

        CreateConfigurationCommand.configurationCreated(type, name, fileCode);
    }

    BatchImportConfig(Long id, String name, String fileCode, String type) {
        this.id = id;
        this.name = name;
        this.fileCode = fileCode;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFileCode() {
        return fileCode;
    }

    public String getType() {
        return type;
    }

    public static BatchImportConfig create(String type, String name, String fileCode) {
        return new BatchImportConfig(type, name, fileCode);
    }
}
