package com.jongsoft.finance.rest.transaction;

import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Introspected
@NoArgsConstructor
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
