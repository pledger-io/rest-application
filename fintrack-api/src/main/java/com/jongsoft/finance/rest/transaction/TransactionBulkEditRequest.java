package com.jongsoft.finance.rest.transaction;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
@Serdeable.Deserializable
public class TransactionBulkEditRequest {

    @Serdeable.Deserializable
    @Schema(name = "EntityRef")
    public record EntityRef(
            @NotNull
            @Schema(description = "The unique identifier of the entity.")
            Long id,
            String name) {}

    @NotNull
    @Size(min = 1)
    @Schema(
            description = "A list of all transaction identifiers that should be updated.",
            minLength = 1)
    private List<Long> transactions;

    @Schema(description = "The contract to set to all transactions")
    private EntityRef contract;
    @Schema(description = "The budget expense to set to all transactions")
    private EntityRef budget;
    @Schema(description = "The category to set to all transactions")
    private EntityRef category;
    @Schema(description = "The list of tags to set to the transactions")
    private List<String> tags;

    public List<Long> getTransactions() {
        return transactions;
    }

    public EntityRef getContract() {
        return contract;
    }

    public EntityRef getBudget() {
        return budget;
    }

    public EntityRef getCategory() {
        return category;
    }

    public List<String> getTags() {
        return tags;
    }
}
