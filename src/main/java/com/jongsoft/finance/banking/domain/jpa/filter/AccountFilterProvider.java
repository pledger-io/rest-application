package com.jongsoft.finance.banking.domain.jpa.filter;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Singleton;

@Singleton
class AccountFilterProvider implements FilterProvider<AccountProvider.FilterCommand> {
    @Override
    public AccountProvider.FilterCommand create() {
        return new AccountFilterCommand();
    }
}
