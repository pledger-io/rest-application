package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.user.Category;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.jsonschema.JsonSchema;
import io.micronaut.serde.annotation.Serdeable;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@Serdeable
@JsonSchema(title = "Category", description = "Category of a transaction", uri = "/category")
public class CategoryJson implements Serializable {

    @NonNull private String label;

    private String description;

    public static CategoryJson fromDomain(Category category) {
        return CategoryJson.builder()
                .label(category.getLabel())
                .description(category.getDescription())
                .build();
    }
}
