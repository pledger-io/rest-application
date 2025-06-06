package com.jongsoft.finance.rest.transaction;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable
record TransactionForSuggestionRequest(
    @Schema(description = "The source account name.") String source,
    @Schema(description = "The destination account name.") String destination,
    @Schema(description = "The amount of the transaction.") Double amount,
    @Schema(description = "The description of the transaction.") String description) {}
