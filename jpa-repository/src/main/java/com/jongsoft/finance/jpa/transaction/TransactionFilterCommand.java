package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.jpa.core.FilterCommandJpa;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.time.Range;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class TransactionFilterCommand extends FilterCommandJpa implements TransactionProvider.FilterCommand {

    private static Function<Iterable<? extends AggregateBase>, List<Long>> ID_REDUCER =
            input -> Collections.List(input).map(AggregateBase::getId).toJava();

    private int pageSize;
    private int page;

    public TransactionFilterCommand() {
        this.pageSize = Integer.MAX_VALUE;
        this.page = 0;
    }

    @Override
    public FilterCommandJpa user(String username) {
        hql("user", " AND a.user.username = :username");
        parameter("username", username);
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand accounts(Sequence<EntityRef> value) {
        hql("accounts", "  AND t.account.id in (:accounts)");
        parameter("accounts", ID_REDUCER.apply(value));
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand categories(Sequence<EntityRef> value) {
        hql("categories", " AND a.category.id in (:categories)");
        parameter("categories", ID_REDUCER.apply(value));
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand contracts(Sequence<EntityRef> value) {
        hql("contracts", " AND a.contract.id in (:contracts)");
        parameter("contracts", ID_REDUCER.apply(value));
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand expenses(Sequence<EntityRef> value) {
        hql("expenses", " AND a.budget.id in (:expenses)");
        parameter("expenses", ID_REDUCER.apply(value));
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand name(String value, boolean exact) {
        if (exact) {
            hql("name", """
                      AND exists (select 1 from TransactionJpa j
                                  where j.journal = t.journal
                                  and j.deleted is null
                                  and lower(j.account.name) = lower(:accountName))""");
            parameter("accountName", value);
        } else {
            hql("name", """
                      AND exists (select 1 from TransactionJpa j
                                  where j.journal = t.journal
                                  and j.deleted is null
                                  and lower(j.account.name) like lower(:accountName))""");
            parameter("accountName", "%" + value + "%");
        }
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand description(String value, boolean exact) {
        hql("description", " AND (lower(a.description) like :description OR lower(t.description) like :description) ");
        parameter("description", "%" + value.toLowerCase() + "%");
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand range(Range<LocalDate> range) {
        hql("date", " AND a.date >= :startDate and a.date < :endDate");
        parameter("startDate", range.from());
        parameter("endDate", range.until());
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand importSlug(String value) {
        hql("batch", " AND a.batchImport.slug = :batchSlug");
        parameter("batchSlug", value);
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand currency(String currency) {
        hql("currency", " AND a.currency.code = :currency");
        parameter("currency", currency);
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand onlyIncome(boolean onlyIncome) {
        if (onlyIncome) {
            hql("income", " AND t.amount > 0");
        } else {
            hql("income", " AND t.amount < 0");
        }

        return this;
    }

    @Override
    public TransactionProvider.FilterCommand ownAccounts() {
        hql("accounts", """
                AND t.account.type.label not in (:systemAccountTypes)
                AND exists (select 1 from TransactionJpa j
                                where j.journal = t.journal
                                and j.deleted is null
                                and j.account.type.label in (:systemAccountTypes)) """);
        parameter("systemAccountTypes", Collections.List(SystemAccountTypes.values()).map(SystemAccountTypes::label).toJava());
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand transfers() {
        hql("accounts", """
                AND not exists (select 1 from TransactionJpa j
                                where j.journal = t.journal
                                and j.deleted is null
                                and j.account.type.label in (:systemAccountTypes)) """);
        parameter("systemAccountTypes", Collections.List(SystemAccountTypes.values()).map(SystemAccountTypes::label).toJava());
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand page(int value) {
        page = value;
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand pageSize(int value) {
        pageSize = value;
        return this;
    }

    @Override
    public Sort sort() {
        return new Sort("a.date",false);
    }

    public int page() {
        return page;
    }

    public int pageSize() {
        return pageSize;
    }

    @Override
    protected String fromHql() {
        return " from TransactionJournal a join a.transactions t where a.deleted is null and t.deleted is null";
    }

}
