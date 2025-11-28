package com.jongsoft.finance.providers;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.lang.collection.Sequence;

public interface TransactionScheduleProvider extends DataProvider<ScheduledTransaction> {

    interface FilterCommand {
        FilterCommand contract(Sequence<EntityRef> contracts);

        FilterCommand activeOnly();
    }

    Sequence<ScheduledTransaction> lookup();

    ResultPage<ScheduledTransaction> lookup(FilterCommand filterCommand);

    @Override
    default boolean supports(Class<?> supportingClass) {
        return ScheduledTransaction.class.equals(supportingClass);
    }
}
