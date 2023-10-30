package com.jongsoft.finance.rest.importer;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Serdeable.Deserializable
class ImporterCreateRequest {

    @NotNull
    @NotBlank
    private String configuration;

    @NotNull
    @NotBlank
    private String uploadToken;

    public String getConfiguration() {
        return configuration;
    }

    public String getUploadToken() {
        return uploadToken;
    }

}
