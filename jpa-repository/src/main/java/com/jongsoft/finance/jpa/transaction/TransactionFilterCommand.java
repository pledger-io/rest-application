package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.jpa.query.expression.Expressions;
import com.jongsoft.finance.jpa.query.expression.FieldEquation;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.time.Range;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class TransactionFilterCommand extends JpaFilterBuilder<TransactionJournal>
        implements TransactionProvider.FilterCommand {

    private static final Function<Iterable<? extends AggregateBase>, List<Long>> ID_REDUCER =
            input -> Collections.List(input).map(AggregateBase::getId).toJava();

    public TransactionFilterCommand() {
        query().fieldNull("deleted").condition(Expressions.fieldNull("t", "deleted"));
        orderAscending = false;
        orderBy = "date";
    }

    @Override
    public void user(String username) {
        query().fieldEq("user.username", username);
    }

    @Override
    public TransactionProvider.FilterCommand accounts(Sequence<EntityRef> value) {
        query().condition(Expressions.fieldCondition(
                "t", "account.id", FieldEquation.IN, ID_REDUCER.apply(value)));
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand categories(Sequence<EntityRef> value) {
        query().fieldEqOneOf("category.id", ID_REDUCER.apply(value).toArray());
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand contracts(Sequence<EntityRef> value) {
        query().fieldEqOneOf("contract.id", ID_REDUCER.apply(value).toArray());
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand expenses(Sequence<EntityRef> value) {
        query().fieldEqOneOf("budget.id", ID_REDUCER.apply(value).toArray());
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand name(String value, boolean exact) {
        query().whereExists(subQuery -> {
            subQuery.fieldNull("deleted").from("transactions");
            if (exact) {
                subQuery.fieldEq("account.name", value);
            } else {
                subQuery.fieldLike("account.name", value.toLowerCase());
            }
        });
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand description(String value, boolean exact) {
        query().condition(Expressions.or(
                Expressions.fieldLike("e", "description", value.toLowerCase()),
                Expressions.fieldLike("t", "description", value.toLowerCase())));

        return this;
    }

    @Override
    public TransactionProvider.FilterCommand range(Range<LocalDate> range) {
        query().condition(Expressions.and(
                Expressions.fieldCondition("e", "date", FieldEquation.GTE, range.from()),
                Expressions.fieldCondition("e", "date", FieldEquation.LT, range.until())));
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand importSlug(String value) {
        query().fieldEq("batchImport.slug", value);
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand currency(String currency) {
        query().fieldEq("currency.code", currency);
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand onlyIncome(boolean onlyIncome) {
        query().condition(Expressions.fieldCondition(
                "t", "amount", onlyIncome ? FieldEquation.GTE : FieldEquation.LTE, 0));
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand ownAccounts() {
        var types = Arrays.stream(SystemAccountTypes.values())
                .map(SystemAccountTypes::label)
                .toArray();
        query().condition(Expressions.fieldCondition(
                        "t", "account.type.label", FieldEquation.NIN, Arrays.asList(types)))
                .whereExists(subQuery -> subQuery.from("transactions")
                        .fieldEqParentField("journal.id", "id")
                        .fieldNull("deleted")
                        .fieldEqOneOf("account.type.label", types));
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand transfers() {
        var types = Arrays.stream(SystemAccountTypes.values())
                .map(SystemAccountTypes::label)
                .toArray();

        query().whereNotExists(subQuery -> subQuery.from("transactions")
                .fieldNull("deleted")
                .fieldEqOneOf("account.type.label", types));
        return this;
    }

    @Override
    public TransactionProvider.FilterCommand page(int value, int pageSize) {
        this.limitRows = pageSize;
        this.skipRows = value * pageSize;
        return this;
    }

    @Override
    public Class<TransactionJournal> entityType() {
        return TransactionJournal.class;
    }
}
