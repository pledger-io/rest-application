package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;
import io.reactivex.Maybe;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionProvider extends DataProvider<Transaction> {

    interface FilterCommand {
        FilterCommand accounts(Sequence<EntityRef> value);
        FilterCommand categories(Sequence<EntityRef> value);
        FilterCommand contracts(Sequence<EntityRef> value);
        FilterCommand expenses(Sequence<EntityRef> value);

        FilterCommand name(String value, boolean exact);
        FilterCommand description(String value, boolean exact);
        FilterCommand range(Range<LocalDate> range);
        FilterCommand importSlug(String value);
        FilterCommand currency(String currency);

        FilterCommand onlyIncome(boolean onlyIncome);
        FilterCommand ownAccounts();
        FilterCommand transfers();

        FilterCommand page(int value);
        FilterCommand pageSize(int value);
    }

    interface DailySummary {
        LocalDate day();
        double summary();
    }

    Maybe<Transaction> first(FilterCommand filter);

    ResultPage<Transaction> lookup(FilterCommand filter);

    Sequence<DailySummary> daily(FilterCommand filter);
    Optional<BigDecimal> balance(FilterCommand filter);

    Sequence<Transaction> similar(EntityRef from, EntityRef to, double amount, LocalDate date);

    default boolean supports(Class<Transaction> supportingClass) {
        return Transaction.class.equals(supportingClass);
    }
}
