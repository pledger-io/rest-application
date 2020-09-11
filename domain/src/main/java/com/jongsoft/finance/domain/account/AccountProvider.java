package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.domain.core.DataProvider;
import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.core.Exportable;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface AccountProvider extends DataProvider<Account>, Exportable<Account> {

    interface FilterCommand {
        FilterCommand name(String value, boolean fullMatch);
        FilterCommand iban(String value, boolean fullMatch);
        FilterCommand number(String value, boolean fullMatch);
        FilterCommand types(Sequence<String> types);

        FilterCommand page(int value);
        FilterCommand pageSize(int value);
    }

    interface AccountSpending {
        Account account();
        double total();
        double average();
    }

    Optional<Account> synonymOf(String synonym);

    Optional<Account> lookup(String name);

    Optional<Account> lookup(SystemAccountTypes accountType);

    ResultPage<Account> lookup(FilterCommand filter);

    Sequence<AccountSpending> top(FilterCommand filter, DateRange range);

    @Override
    default boolean supports(Class<Account> supportingClass) {
        return Account.class.equals(supportingClass);
    }
}
