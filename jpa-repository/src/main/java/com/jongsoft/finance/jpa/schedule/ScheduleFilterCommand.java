package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.lang.collection.Sequence;

import java.time.LocalDate;

public class ScheduleFilterCommand extends JpaFilterBuilder<ScheduledTransactionJpa>
        implements TransactionScheduleProvider.FilterCommand {

    public ScheduleFilterCommand() {
        orderAscending = false;
        orderBy = "start";
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
    public Class<ScheduledTransactionJpa> entityType() {
        return ScheduledTransactionJpa.class;
    }
}
