package com.jongsoft.finance.rest.category;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor
class CategoryCreateRequest {

    @NotNull
    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 1024)
    private String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
