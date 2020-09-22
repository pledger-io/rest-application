package com.jongsoft.finance.rest.importer;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

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
