package com.jongsoft.finance.banking.adapter.api;

import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface TransactionScheduleProvider {

    interface FilterCommand {
        FilterCommand contract(Sequence<EntityRef> contracts);

        FilterCommand account(Sequence<EntityRef> account);

        FilterCommand activeOnly();
    }

    Sequence<TransactionSchedule> lookup();

    Optional<TransactionSchedule> lookup(long id);

    ResultPage<TransactionSchedule> lookup(FilterCommand filterCommand);
}
