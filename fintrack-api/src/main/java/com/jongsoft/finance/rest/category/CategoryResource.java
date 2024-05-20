package com.jongsoft.finance.rest.category;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.model.CategoryResponse;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

@Tag(name = "Category")
@Controller("/api/categories")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class CategoryResource {

    private final FilterFactory filterFactory;
    private final CategoryProvider categoryService;
    private final CurrentUserProvider currentUserProvider;

    private final SettingProvider settingProvider;

    public CategoryResource(
            FilterFactory filterFactory,
            CategoryProvider categoryService,
            CurrentUserProvider currentUserProvider,
            SettingProvider settingProvider) {
        this.filterFactory = filterFactory;
        this.categoryService = categoryService;
        this.currentUserProvider = currentUserProvider;
        this.settingProvider = settingProvider;
    }

    @Get
    @Operation(
            summary = "List categories",
            description = "List all available categories",
            operationId = "getAll"
    )
    List<CategoryResponse> list() {
        return categoryService.lookup()
                .map(CategoryResponse::new)
                .toJava();
    }

    @Post
    @Operation(
            summary = "Search categories",
            description = "Search through the categories with the provided filter set",
            operationId = "searchCategories"
    )
    ResultPageResponse<CategoryResponse> search(@Valid @Body CategorySearchRequest searchRequest) {
        var response = categoryService.lookup(
                filterFactory.category()
                        .page(searchRequest.getPage())
                        .pageSize(settingProvider.getPageSize()));

        return new ResultPageResponse<>(response.map(CategoryResponse::new));
    }

    @Get("/auto-complete{?token}")
    @Operation(
            summary = "Autocomplete",
            description = "List all categories matching the provided token",
            operationId = "getCategoriesByToken"
    )
    List<CategoryResponse> autocomplete(@Nullable String token) {
        return categoryService.lookup(
                        filterFactory.category()
                                .label(token, false)
                                .pageSize(settingProvider.getAutocompleteLimit()))
                .content()
                .map(CategoryResponse::new)
                .toJava();
    }

    @Put
    @Status(HttpStatus.CREATED)
    @Operation(
            summary = "Create category",
            description = "Adds a new category to the system",
            operationId = "createCategory"
    )
    CategoryResponse create(@Valid @Body CategoryCreateRequest createRequest) {
        currentUserProvider.currentUser()
                .createCategory(createRequest.name());

        return categoryService.lookup(createRequest.name())
                .map(category -> {
                    category.rename(createRequest.name(), createRequest.description());
                    return category;
                })
                .map(CategoryResponse::new)
                .getOrThrow(() -> StatusException.internalError("Could not create category"));
    }

    @Get("/{id}")
    @Operation(
            summary = "Get category",
            description = "Get a single category by its Id",
            operationId = "getCategory"
    )
    CategoryResponse get(@PathVariable long id) {
        return categoryService.lookup(id)
                .map(CategoryResponse::new)
                .getOrThrow(() -> StatusException.notFound("No category found with id " + id));
    }

    @Post("/{id}")
    @Operation(
            summary = "Update category",
            description = "Update a single category by its Id",
            operationId = "updateCategory"
    )
    CategoryResponse update(
            @PathVariable long id,
            @Valid @Body CategoryCreateRequest updateRequest) {
        return categoryService.lookup(id)
                .map(category -> {
                    category.rename(updateRequest.name(), updateRequest.description());
                    return new CategoryResponse(category);
                })
                .getOrThrow(() -> StatusException.notFound("No category found with id " + id));
    }

    @Delete("/{id}")
    @Operation(
            summary = "Delete category",
            description = "Delete a single category by its Id",
            operationId = "deleteCategory"
    )
    @Status(HttpStatus.NO_CONTENT)
    void delete(@PathVariable long id) {
        categoryService.lookup(id)
                .ifPresent(Category::remove);
    }

}
