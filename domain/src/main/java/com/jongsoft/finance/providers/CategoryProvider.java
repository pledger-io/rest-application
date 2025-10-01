package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.lang.control.Optional;

public interface CategoryProvider extends DataProvider<Category>, Exportable<Category> {

    interface FilterCommand {
        FilterCommand label(String label, boolean exact);

        FilterCommand page(int page, int pageSize);
    }

    Optional<Category> lookup(String label);

    ResultPage<Category> lookup(FilterCommand filterCommand);

    @Override
    default boolean supports(Class<?> supportingClass) {
        return Category.class.equals(supportingClass);
    }
}
