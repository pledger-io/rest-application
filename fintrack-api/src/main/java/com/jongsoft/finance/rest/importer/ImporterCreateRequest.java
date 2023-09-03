package com.jongsoft.finance.rest.importer;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Introspected
class ImporterCreateRequest {

    @NotNull
    @NotBlank
    private String configuration;

    @NotNull
    @NotBlank
    private String uploadToken;

    public ImporterCreateRequest() {
        // left blank intentionally for deserializer
    }

    @Builder
    private ImporterCreateRequest(String configuration, String uploadToken) {
        this.configuration = configuration;
        this.uploadToken = uploadToken;
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getUploadToken() {
        return uploadToken;
    }

}
