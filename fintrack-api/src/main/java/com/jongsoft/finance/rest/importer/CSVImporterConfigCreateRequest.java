package com.jongsoft.finance.rest.importer;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
class CSVImporterConfigCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String fileCode;

}
