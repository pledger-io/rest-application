package com.jongsoft.finance.budget.adapter.api;

import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.lang.control.Optional;

public interface ExpenseProvider {

    interface FilterCommand {
        FilterCommand name(String value, boolean exact);
    }

    Optional<EntityRef.NamedEntity> lookup(long id);

    ResultPage<EntityRef.NamedEntity> lookup(FilterCommand filter);
}
