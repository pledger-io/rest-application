package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.lang.collection.Sequence;
import io.reactivex.Maybe;
import io.reactivex.Single;

public interface BudgetProvider extends Exportable<Budget> {

    @Override
    Sequence<Budget> lookup();

    Single<Budget> lookup(int year, int month);

    Maybe<Budget> first();

    @Override
    default boolean supports(Class<Budget> supportingClass) {
        return Budget.class.equals(supportingClass);
    }
}
