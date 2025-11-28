package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.rest.model.CategoryResponse;
import com.jongsoft.finance.rest.model.PagedCategoryResponse;
import com.jongsoft.finance.rest.model.PagedResponseInfo;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class CategoryFetcherController implements CategoryFetcherApi {

    private final Logger logger;

    private final CategoryProvider categoryProvider;
    private final FilterFactory filterFactory;

    public CategoryFetcherController(
            CategoryProvider categoryProvider, FilterFactory filterFactory) {
        this.categoryProvider = categoryProvider;
        this.filterFactory = filterFactory;
        this.logger = LoggerFactory.getLogger(CategoryFetcherController.class);
    }

    @Override
    public PagedCategoryResponse findCategoriesBy(
            Integer offset, Integer numberOfResults, String name) {
        logger.info("Fetching all categories, with provided filters.");
        var page = offset / numberOfResults;
        var filter = filterFactory.category().page(Math.max(0, page), Math.max(1, numberOfResults));
        if (name != null) {
            filter.label(name, false);
        }

        var results = categoryProvider.lookup(filter);

        return new PagedCategoryResponse(
                new PagedResponseInfo(results.total(), results.pages(), results.pageSize()),
                results.content().map(CategoryMapper::toCategoryResponse).toJava());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        logger.info("Fetching category {}.", id);

        var category = categoryProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Category not found"));

        if (category.isDelete()) {
            throw StatusException.gone("Category has been removed from the system");
        }

        return CategoryMapper.toCategoryResponse(category);
    }
}
