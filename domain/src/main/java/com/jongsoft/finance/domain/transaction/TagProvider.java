package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.domain.core.Exportable;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.lang.control.Optional;

public interface TagProvider extends Exportable<Tag> {

    interface FilterCommand {
        FilterCommand name(String value, boolean exact);
        FilterCommand page(int page);
        FilterCommand pageSize(int pageSize);
    }

    Optional<Tag> lookup(String name);
    ResultPage<Tag> lookup(FilterCommand filter);

}
