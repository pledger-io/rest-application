package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.lang.control.Optional;

public interface TagProvider extends Exportable<Tag> {

    interface FilterCommand {
        FilterCommand name(String value, boolean exact);
        FilterCommand page(int page, int pageSize);
    }

    Optional<Tag> lookup(String name);
    ResultPage<Tag> lookup(FilterCommand filter);

    @Override
    default boolean supports(Class<?> supportingClass) {
        return Tag.class.equals(supportingClass);
    }
}
