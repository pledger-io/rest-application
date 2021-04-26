package com.jongsoft.finance.automation;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.TerminateAccountCommand;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.time.LocalDate;

/**
 * This class will automatically terminate all active transaction schedules attached to the contract that was
 * terminated. This will prevent new transactions from being made after a contract is deleted from the system.
 */
@Slf4j
@Singleton
class ScheduleTerminateContractHandler implements CommandHandler<TerminateAccountCommand> {

    private final TransactionScheduleProvider transactionScheduleProvider;
    private final FilterFactory filterFactory;

    ScheduleTerminateContractHandler(TransactionScheduleProvider transactionScheduleProvider, FilterFactory filterFactory) {
        this.transactionScheduleProvider = transactionScheduleProvider;
        this.filterFactory = filterFactory;
    }

    @Override
    public void handle(TerminateAccountCommand command) {
        log.trace("[{}] - Terminating any transaction schedule for contract.", command.id());

        var filter = filterFactory.schedule()
                .contract(Collections.List(new EntityRef(command.id())))
                .activeOnly();

        transactionScheduleProvider.lookup(filter)
                .content()
                .forEach(schedule ->
                        schedule.limit(
                                schedule.getStart(),
                                LocalDate.now()));
    }

}
