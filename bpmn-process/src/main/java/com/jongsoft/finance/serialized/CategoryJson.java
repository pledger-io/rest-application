package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.user.Category;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
@Serdeable
public class CategoryJson implements Serializable {

    private String label;
    private String description;

    public static CategoryJson fromDomain(Category category) {
        return CategoryJson.builder()
                .label(category.getLabel())
                .description(category.getDescription())
                .build();
    }

}
