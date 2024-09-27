package com.jongsoft.finance.rest.contract;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Serdeable.Deserializable
record ContractCreateRequest (
        @NotNull
        @NotBlank
        @Schema(description = "The name of the contract.", example = "Contract 1")
        String name,

        @Schema(description = "The description of the contract.", example = "Contract 1 description")
        String description,

        @NotNull
        @Schema(description = "The company the contract is with.")
        EntityRef company,

        @NotNull
        @Schema(description = "The start date of the contract.")
        LocalDate start,

        @NotNull
        @Schema(description = "The end date of the contract.")
        LocalDate end) {

    @Serdeable.Deserializable
    public record EntityRef(
            @NotNull
            @Schema(description = "The id of the company.", example = "1")
            Long id,
            @Schema(description = "The name of the company.", example = "Company 1")
            String name){
    }

}
