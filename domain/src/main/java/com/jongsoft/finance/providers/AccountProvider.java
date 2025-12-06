package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;

import java.time.LocalDate;

public interface AccountProvider extends DataProvider<Account>, Exportable<Account> {

    interface FilterCommand {
        FilterCommand name(String value, boolean fullMatch);

        FilterCommand iban(String value, boolean fullMatch);

        FilterCommand number(String value, boolean fullMatch);

        FilterCommand types(Sequence<String> types);

        FilterCommand page(int page, int pageSize);
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

    Sequence<AccountSpending> top(FilterCommand filter, Range<LocalDate> range, boolean asc);

    @Override
    default boolean supports(Class<?> supportingClass) {
        return Account.class.equals(supportingClass);
    }

    @Override
    default String typeOf() {
        return "ACCOUNT";
    }
}
