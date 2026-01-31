package com.jongsoft.finance.banking.domain.jpa.filter;

import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionScheduleJpa;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.core.domain.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.core.domain.jpa.query.expression.Expressions;
import com.jongsoft.finance.core.domain.jpa.query.expression.FieldEquation;
import com.jongsoft.lang.collection.Sequence;

import java.time.LocalDate;

public class TransactionScheduleFilterCommand extends JpaFilterBuilder<TransactionScheduleJpa>
        implements TransactionScheduleProvider.FilterCommand {

    public TransactionScheduleFilterCommand() {
        orderAscending = false;
        orderBy = "start";
    }

    @Override
    public TransactionScheduleProvider.FilterCommand account(Sequence<EntityRef> accounts) {
        if (!accounts.isEmpty()) {
            query().condition(Expressions.or(
                    Expressions.fieldCondition(
                            "t",
                            "source.id",
                            FieldEquation.IN,
                            accounts.map(EntityRef::getId).toJava()),
                    Expressions.fieldCondition(
                            "t",
                            "destination.id",
                            FieldEquation.IN,
                            accounts.map(EntityRef::getId).toJava())));
        }
        return this;
    }

    @Override
    public TransactionScheduleProvider.FilterCommand contract(Sequence<EntityRef> contracts) {
        if (!contracts.isEmpty()) {
            query().fieldEqOneOf(
                            "contract.id",
                            contracts.map(EntityRef::getId).toJava().toArray());
        }
        return this;
    }

    @Override
    public TransactionScheduleProvider.FilterCommand activeOnly() {
        query().fieldGtOrEqNullable("end", LocalDate.now());
        return this;
    }

    public void user(String username) {
        query().fieldEq("user.username", username);
    }

    @Override
    public Class<TransactionScheduleJpa> entityType() {
        return TransactionScheduleJpa.class;
    }
}
