package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.domain.core.Exportable;
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
