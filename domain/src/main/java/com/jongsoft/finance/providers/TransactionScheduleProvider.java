package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.lang.collection.Sequence;

public interface TransactionScheduleProvider extends DataProvider<ScheduledTransaction> {

    Sequence<ScheduledTransaction> lookup();

    @Override
    default boolean supports(Class<ScheduledTransaction> supportingClass) {
        return ScheduledTransaction.class.equals(supportingClass);
    }
}
