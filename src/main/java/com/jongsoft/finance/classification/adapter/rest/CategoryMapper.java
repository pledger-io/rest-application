package com.jongsoft.finance.classification.adapter.rest;

import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.rest.model.CategoryResponse;

public interface CategoryMapper {

    static CategoryResponse toCategoryResponse(Category category) {
        var response = new CategoryResponse(category.getId());
        response.setDescription(category.getDescription());
        response.setName(category.getLabel());
        return response;
    }
}
