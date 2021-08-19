package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.transaction.Tag;
import reactor.core.publisher.Mono;

public interface TagProvider extends Exportable<Tag> {

    interface FilterCommand {
        FilterCommand name(String value, boolean exact);
        FilterCommand page(int page);
        FilterCommand pageSize(int pageSize);
    }

    Mono<Tag> lookup(String name);
    ResultPage<Tag> lookup(FilterCommand filter);

    @Override
    default boolean supports(Class<Tag> supportingClass) {
        return Tag.class.equals(supportingClass);
    }
}
