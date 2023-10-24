package com.jongsoft.finance.rest.transaction;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Serdeable.Deserializable
class GroupRenameRequest {

    @NotNull
    @NotBlank
    @Schema(description = "The new name of the group", example = "My renamed group")
    private String name;

    GroupRenameRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
