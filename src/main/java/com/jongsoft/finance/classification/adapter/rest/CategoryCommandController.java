package com.jongsoft.finance.classification.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.rest.CategoryCommandApi;
import com.jongsoft.finance.rest.model.CategoryRequest;
import com.jongsoft.finance.rest.model.CategoryResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
class CategoryCommandController implements CategoryCommandApi {

    private final Logger logger;

    private final CategoryProvider categoryProvider;

    public CategoryCommandController(CategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
        this.logger = LoggerFactory.getLogger(CategoryCommandController.class);
    }

    @Override
    public HttpResponse<@Valid CategoryResponse> createCategory(CategoryRequest categoryRequest) {
        logger.info("Creating category {}.", categoryRequest.getName());

        Category.create(categoryRequest.getName(), categoryRequest.getDescription());
        var category = categoryProvider
                .lookup(categoryRequest.getName())
                .getOrThrow(() -> StatusException.internalError("Failed to create category"));

        return HttpResponse.created(CategoryMapper.toCategoryResponse(category));
    }

    @Override
    public HttpResponse<Void> deleteCategoryById(Long id) {
        logger.info("Deleting category {}.", id);

        lookupCategoryOrThrow(id).remove();
        return HttpResponse.noContent();
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        logger.info("Updating category {}.", id);
        var category = lookupCategoryOrThrow(id);
        category.rename(categoryRequest.getName(), categoryRequest.getDescription());
        return CategoryMapper.toCategoryResponse(category);
    }

    private Category lookupCategoryOrThrow(long id) {
        return categoryProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("No category found with id " + id));
    }
}
