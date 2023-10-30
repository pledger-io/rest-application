package com.jongsoft.finance.rest.importer;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Serdeable.Deserializable
class CSVImporterConfigCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String fileCode;

    public String getName() {
        return name;
    }

    public String getFileCode() {
        return fileCode;
    }
}
