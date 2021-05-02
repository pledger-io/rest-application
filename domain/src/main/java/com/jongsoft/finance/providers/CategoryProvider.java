package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Category;
import io.reactivex.Maybe;

public interface CategoryProvider extends DataProvider<Category>, Exportable<Category> {

    interface FilterCommand {
        FilterCommand label(String label, boolean exact);
        FilterCommand page(int page);
        FilterCommand pageSize(int pageSize);
    }

    Maybe<Category> lookup(String label);

    ResultPage<Category> lookup(FilterCommand filterCommand);

    @Override
    default boolean supports(Class<Category> supportingClass) {
        return Category.class.equals(supportingClass);
    }
}