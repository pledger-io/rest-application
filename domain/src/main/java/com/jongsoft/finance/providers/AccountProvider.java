package com.jongsoft.finance.providers;

import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.time.Range;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

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

    Mono<Account> synonymOf(String synonym);

    Mono<Account> lookup(String name);

    Mono<Account> lookup(SystemAccountTypes accountType);

    ResultPage<Account> lookup(FilterCommand filter);

    Sequence<AccountSpending> top(FilterCommand filter, Range<LocalDate> range, boolean asc);

    @Override
    default boolean supports(Class<Account> supportingClass) {
        return Account.class.equals(supportingClass);
    }
}
