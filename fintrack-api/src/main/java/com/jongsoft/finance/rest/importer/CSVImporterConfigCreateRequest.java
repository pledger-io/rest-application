package com.jongsoft.finance.rest.importer;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Introspected
@NoArgsConstructor
class CSVImporterConfigCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String fileCode;

    @Builder
    CSVImporterConfigCreateRequest(String name, String fileCode) {
        this.name = name;
        this.fileCode = fileCode;
    }

    public String getName() {
        return name;
    }

    public String getFileCode() {
        return fileCode;
    }
}
