package com.jongsoft.finance.rest.contract;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Serdeable.Deserializable
class ContractCreateRequest {

    @Builder
    @Serdeable.Deserializable
    static class EntityRef {
        @NotNull
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @NotNull
    @NotBlank
    @Schema(description = "The name of the contract.", example = "Contract 1")
    private String name;

    @Schema(description = "The description of the contract.", example = "Contract 1 description")
    private String description;

    @NotNull
    @Schema(description = "The company the contract is with.")
    private EntityRef company;

    @NotNull
    @Schema(description = "The start date of the contract.")
    private LocalDate start;

    @NotNull
    @Schema(description = "The end date of the contract.")
    private LocalDate end;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EntityRef getCompany() {
        return company;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }
}
