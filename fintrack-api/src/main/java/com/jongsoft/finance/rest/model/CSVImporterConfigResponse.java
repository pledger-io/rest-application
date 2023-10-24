package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.importer.BatchImportConfig;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable.Serializable
public class CSVImporterConfigResponse {

    private final BatchImportConfig wrapped;

    public CSVImporterConfigResponse(BatchImportConfig wrapped) {
        this.wrapped = wrapped;
    }

    @Schema(description = "The CSV configuration identifier")
    public Long getId() {
        return wrapped.getId();
    }

    @Schema(description = "The name of the CSV configuration")
    public String getName() {
        return wrapped.getName();
    }

    @Schema(description = "The file code to get the contents of the configuration")
    public String getFile() {
        return wrapped.getFileCode();
    }
}
