package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.domain.core.Exportable;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Single;

public interface BudgetProvider extends Exportable<Budget> {

    @Override
    Sequence<Budget> lookup();

    Single<Budget> lookup(int year, int month);

    Optional<Budget> first();

}
