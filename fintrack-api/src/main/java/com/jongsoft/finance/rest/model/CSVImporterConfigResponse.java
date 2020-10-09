package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.importer.BatchImportConfig;

public class CSVImporterConfigResponse {

    private final BatchImportConfig wrapped;

    public CSVImporterConfigResponse(BatchImportConfig wrapped) {
        this.wrapped = wrapped;
    }

    public Long getId() {
        return wrapped.getId();
    }

    public String getName() {
        return wrapped.getName();
    }

    public String getFile() {
        return wrapped.getFileCode();
    }
}
