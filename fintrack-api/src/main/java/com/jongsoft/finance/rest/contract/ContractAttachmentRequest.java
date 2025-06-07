package com.jongsoft.finance.rest.contract;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable.Deserializable
public record ContractAttachmentRequest(
    @Schema(description = "The file code of the attachment.", example = "1234567890")
        String fileCode) {}
