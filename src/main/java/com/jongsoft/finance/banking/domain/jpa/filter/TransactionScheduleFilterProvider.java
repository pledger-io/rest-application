package com.jongsoft.finance.banking.domain.jpa.filter;

import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Singleton;

@Singleton
class TransactionScheduleFilterProvider
        implements FilterProvider<TransactionScheduleProvider.FilterCommand> {
    @Override
    public TransactionScheduleProvider.FilterCommand create() {
        return new TransactionScheduleFilterCommand();
    }
}
