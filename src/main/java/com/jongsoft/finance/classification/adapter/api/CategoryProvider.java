package com.jongsoft.finance.classification.adapter.api;

import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface CategoryProvider {

    interface FilterCommand {
        FilterCommand label(String label, boolean exact);

        FilterCommand page(int page, int pageSize);
    }

    Sequence<Category> lookup();

    Optional<Category> lookup(long id);

    Optional<Category> lookup(String label);

    ResultPage<Category> lookup(FilterCommand filterCommand);
}
