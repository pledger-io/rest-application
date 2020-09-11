package com.jongsoft.finance.serialized;

import java.io.Serializable;

import com.jongsoft.finance.domain.user.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
