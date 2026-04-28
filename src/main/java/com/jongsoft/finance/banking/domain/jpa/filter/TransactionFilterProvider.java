package com.jongsoft.finance.banking.domain.jpa.filter;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.annotations.BankingModuleEnabled;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Singleton;

@Singleton
@BankingModuleEnabled
class TransactionFilterProvider implements FilterProvider<TransactionProvider.FilterCommand> {
    @Override
    public TransactionProvider.FilterCommand create() {
        return new TransactionFilterCommand();
    }
}
