package com.jongsoft.finance.providers;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;

public interface ExpenseProvider extends DataProvider<EntityRef.NamedEntity> {

    interface FilterCommand {
        FilterCommand name(String value, boolean exact);
    }

    ResultPage<EntityRef.NamedEntity> lookup(FilterCommand filter);

    default boolean supports(Class<EntityRef.NamedEntity> supportingClass) {
        return EntityRef.NamedEntity.class.equals(supportingClass);
    }
}
