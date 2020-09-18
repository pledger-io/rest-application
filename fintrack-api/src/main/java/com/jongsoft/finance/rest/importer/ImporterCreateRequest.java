package com.jongsoft.finance.rest.importer;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
class ImporterCreateRequest {

    @NotNull
    @NotBlank
    private String configuration;

    @NotNull
    @NotBlank
    private String uploadToken;

}
