package com.jongsoft.finance.rest.category;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Serdeable.Deserializable
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
