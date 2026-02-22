package com.jongsoft.finance.banking.adapter.api;

import com.jongsoft.finance.banking.domain.model.Tag;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface TagProvider {

    interface FilterCommand {
        FilterCommand name(String value, boolean exact);

        FilterCommand page(int page, int pageSize);
    }

    Sequence<Tag> lookup();

    Optional<Tag> lookup(String name);

    ResultPage<Tag> lookup(FilterCommand filter);
}
