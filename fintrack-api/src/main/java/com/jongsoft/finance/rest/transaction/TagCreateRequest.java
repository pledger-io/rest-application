package com.jongsoft.finance.rest.transaction;

import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * The tag create request is used to add new tags to FinTrack
 */
@Introspected
@NoArgsConstructor
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
