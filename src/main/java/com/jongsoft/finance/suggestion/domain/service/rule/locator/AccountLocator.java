package com.jongsoft.finance.suggestion.domain.service.rule.locator;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.suggestion.domain.service.rule.ChangeLocator;
import com.jongsoft.finance.suggestion.types.RuleColumn;

import jakarta.inject.Singleton;

import java.util.List;

@Singleton
class AccountLocator implements ChangeLocator {

    private static final List<RuleColumn> SUPPORTED = List.of(
            RuleColumn.SOURCE_ACCOUNT,
            RuleColumn.TO_ACCOUNT,
            RuleColumn.CHANGE_TRANSFER_FROM,
            RuleColumn.CHANGE_TRANSFER_TO);

    private final AccountProvider accountProvider;

    public AccountLocator(AccountProvider accountProvider) {
        this.accountProvider = accountProvider;
    }

    @Override
    public Object locate(RuleColumn column, String change) {
        return accountProvider.lookup(Long.parseLong(change)).get();
    }

    @Override
    public boolean supports(RuleColumn column) {
        return SUPPORTED.contains(column);
    }
}
