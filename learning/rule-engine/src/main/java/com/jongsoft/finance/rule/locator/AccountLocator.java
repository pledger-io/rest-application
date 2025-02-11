package com.jongsoft.finance.rule.locator;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.providers.AccountProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AccountLocator implements ChangeLocator {

    private static final List<RuleColumn> SUPPORTED = List.of(
            RuleColumn.SOURCE_ACCOUNT,
            RuleColumn.TO_ACCOUNT,
            RuleColumn.CHANGE_TRANSFER_FROM,
            RuleColumn.CHANGE_TRANSFER_TO);

    private final AccountProvider accountProvider;

    @Override
    public Object locate(RuleColumn column, String change) {
        return accountProvider.lookup(Long.parseLong(change)).get();
    }

    @Override
    public boolean supports(RuleColumn column) {
        return SUPPORTED.contains(column);
    }

}
