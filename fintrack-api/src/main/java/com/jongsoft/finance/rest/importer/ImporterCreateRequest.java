package com.jongsoft.finance.rest.importer;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
class ImporterCreateRequest {

    @NotNull
    @NotBlank
    private String configuration;

    @NotNull
    @NotBlank
    private String uploadToken;

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