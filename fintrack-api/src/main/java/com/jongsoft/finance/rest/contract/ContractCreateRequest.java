package com.jongsoft.finance.rest.contract;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Introspected
@NoArgsConstructor
class ContractCreateRequest {

    @Introspected
    @NoArgsConstructor
    static class EntityRef {
        @NotNull
        private Long id;
        private String name;

        @Builder
        EntityRef(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @NotNull
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private EntityRef company;

    @NotNull
    private LocalDate start;

    @NotNull
    private LocalDate end;

    @Builder
    ContractCreateRequest(String name, String description, EntityRef company, LocalDate start, LocalDate end) {
        this.name = name;
        this.description = description;
        this.company = company;
        this.start = start;
        this.end = end;
    }

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
