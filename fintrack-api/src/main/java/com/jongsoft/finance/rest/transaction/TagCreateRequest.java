package com.jongsoft.finance.rest.transaction;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

/**
 * The tag create request is used to add new tags to FinTrack
 */
@NoArgsConstructor
@Serdeable.Deserializable
public class TagCreateRequest {

    @Schema(
            description = "The name of the tag to be created",
            implementation = String.class,
            example = "Car expenses",
            required = true)
    @NotNull
    @NotBlank
    private String tag;

    public TagCreateRequest(String tag) {
        this.tag = tag;
    }

    /**
     * Get the actual tag name
     */
    public String getTag() {
        return tag;
    }

}
