package com.jongsoft.finance.rest.transaction;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable
record TransactionExtractRequest(
    @Schema(description = "The text to extract the transaction information from.")
        String fromText) {}
