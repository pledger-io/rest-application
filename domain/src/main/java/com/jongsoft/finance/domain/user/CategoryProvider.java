package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.domain.core.DataProvider;
import com.jongsoft.finance.domain.core.Exportable;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.lang.control.Optional;

public interface CategoryProvider extends DataProvider<Category>, Exportable<Category> {

    interface FilterCommand {
        FilterCommand label(String label, boolean exact);
        FilterCommand page(int page);
        FilterCommand pageSize(int pageSize);
    }

    Optional<Category> lookup(String label);

    ResultPage<Category> lookup(FilterCommand filterCommand);

    @Override
    default boolean supports(Class<Category> supportingClass) {
        return Category.class.equals(supportingClass);
    }
}