package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.rest.model.CategoryResponse;

interface CategoryMapper {

    static CategoryResponse toCategoryResponse(Category category) {
        var response = new CategoryResponse(category.getId());
        response.setDescription(category.getDescription());
        response.setName(category.getLabel());
        response.setLastUsed(category.getLastActivity());
        return response;
    }
}
