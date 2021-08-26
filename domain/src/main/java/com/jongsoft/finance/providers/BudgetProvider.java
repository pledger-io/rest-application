package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.lang.collection.Sequence;
import reactor.core.publisher.Mono;

public interface BudgetProvider extends Exportable<Budget> {

    @Override
    Sequence<Budget> lookup();

    Mono<Budget> lookup(int year, int month);

    Mono<Budget> first();

    @Override
    default boolean supports(Class<Budget> supportingClass) {
        return Budget.class.equals(supportingClass);
    }
}
