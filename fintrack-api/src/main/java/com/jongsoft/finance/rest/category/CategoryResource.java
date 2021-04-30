package com.jongsoft.finance.rest.category;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.model.CategoryResponse;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;

@Tag(name = "Category")
@Controller("/api/categories")
@Secured(SecurityRule.IS_AUTHENTICATED)
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
    Flowable<CategoryResponse> list() {
        return Flowable.fromIterable(
                categoryService.lookup()
                        .map(CategoryResponse::new));
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
    Flowable<CategoryResponse> autocomplete(@Nullable String token) {
        var response = categoryService.lookup(
                filterFactory.category()
                        .label(token, false)
                        .pageSize(settingProvider.getAutocompleteLimit()))
                .content()
                .map(CategoryResponse::new);

        return Flowable.fromIterable(response);
    }

    @Put
    @Status(HttpStatus.CREATED)
    @Operation(
            summary = "Create category",
            description = "Adds a new category to the system",
            operationId = "createCategory"
    )
    Single<CategoryResponse> create(@Valid @Body CategoryCreateRequest createRequest) {
        currentUserProvider.currentUser()
                .createCategory(createRequest.getName());

        return categoryService.lookup(createRequest.getName())
                .map(category -> {
                    category.rename(createRequest.getName(), createRequest.getDescription());
                    return category;
                })
                .map(CategoryResponse::new)
                .switchIfEmpty(Single.error(StatusException.internalError("Could not create category")));
    }

    @Get("/{id}")
    @Operation(
            summary = "Get category",
            description = "Get a single category by its Id",
            operationId = "getCategory"
    )
    Single<CategoryResponse> get(@PathVariable long id) {
        return Maybe.<Category>create(emitter -> {
            categoryService.lookup(id)
                    .ifPresent(emitter::onSuccess);

            emitter.onComplete();
        })
                .map(CategoryResponse::new)
                .switchIfEmpty(Single.error(StatusException.notFound("No category found with id " + id)));
    }

    @Post("/{id}")
    @Operation(
            summary = "Update category",
            description = "Update a single category by its Id",
            operationId = "updateCategory"
    )
    Single<CategoryResponse> update(
            @PathVariable long id,
            @Valid @Body CategoryCreateRequest updateRequest) {
        return Maybe.
                <Category>create(emitter -> {
                    categoryService.lookup(id)
                            .ifPresent(emitter::onSuccess);

                    emitter.onComplete();
                })
                .map(category -> {
                    category.rename(updateRequest.getName(), updateRequest.getDescription());
                    return category;
                })
                .map(CategoryResponse::new)
                .switchIfEmpty(Single.error(StatusException.notFound("No category found with id " + id)));
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
