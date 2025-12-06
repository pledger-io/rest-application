package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface BudgetProvider extends Exportable<Budget> {

    @Override
    Sequence<Budget> lookup();

    Optional<Budget> lookup(int year, int month);

    Optional<Budget> first();

    @Override
    default boolean supports(Class<?> supportingClass) {
        return Budget.class.equals(supportingClass);
    }

    default String typeOf() {
        return "BUDGET";
    }
}
