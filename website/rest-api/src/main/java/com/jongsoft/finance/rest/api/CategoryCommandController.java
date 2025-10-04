package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.rest.model.CategoryRequest;
import com.jongsoft.finance.rest.model.CategoryResponse;
import com.jongsoft.finance.security.CurrentUserProvider;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class CategoryCommandController implements CategoryCommandApi {

    private final Logger logger;

    private final CurrentUserProvider currentUserProvider;
    private final CategoryProvider categoryProvider;

    public CategoryCommandController(
            CurrentUserProvider currentUserProvider, CategoryProvider categoryProvider) {
        this.currentUserProvider = currentUserProvider;
        this.categoryProvider = categoryProvider;
        this.logger = LoggerFactory.getLogger(CategoryCommandController.class);
    }

    @Override
    public HttpResponse<@Valid CategoryResponse> createCategory(CategoryRequest categoryRequest) {
        logger.info("Creating category {}.", categoryRequest.getName());

        currentUserProvider.currentUser().createCategory(categoryRequest.getName());

        var category = categoryProvider
                .lookup(categoryRequest.getName())
                .getOrThrow(() -> StatusException.internalError("Failed to create category"));
        category.rename(categoryRequest.getName(), categoryRequest.getDescription());

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
