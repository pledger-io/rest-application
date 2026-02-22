package com.jongsoft.finance.banking.adapter.api;

import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.types.SystemAccountTypes;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;

import java.time.LocalDate;

public interface AccountProvider {

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

    Sequence<Account> lookup();

    Optional<Account> lookup(long id);

    Optional<Account> lookup(String name);

    Optional<Account> lookup(SystemAccountTypes accountType);

    ResultPage<Account> lookup(FilterCommand filter);

    Sequence<AccountSpending> top(FilterCommand filter, Range<LocalDate> range, boolean asc);
}
