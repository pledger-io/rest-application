package com.jongsoft.finance.rest.file;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Serializable
class UploadResponse {

    private String fileCode;

    public UploadResponse(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getFileCode() {
        return fileCode;
    }
}
