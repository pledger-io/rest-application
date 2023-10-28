package com.jongsoft.finance.rest.contract;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Serdeable.Deserializable
public class ContractAttachmentRequest {

    @Schema(description = "The file code of the attachment.", example = "1234567890")
    private String fileCode;

    public String getFileCode() {
        return fileCode;
    }

}
