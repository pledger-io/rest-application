package com.jongsoft.finance.automation;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.TerminateContractCommand;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.lang.Collections;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class will automatically terminate all active transaction schedules attached to the contract that was
 * terminated. This will prevent new transactions from being made after a contract is deleted from the system.
 */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ScheduleTerminateContractHandler implements CommandHandler<TerminateContractCommand> {

    private final TransactionScheduleProvider transactionScheduleProvider;
    private final FilterFactory filterFactory;

    @Override
    @BusinessEventListener
    public void handle(TerminateContractCommand command) {
        log.info("[{}] - Terminating any transaction schedule for contract.", command.id());

        var filter = filterFactory.schedule()
                .contract(Collections.List(new EntityRef(command.id())))
                .activeOnly();

        transactionScheduleProvider.lookup(filter)
                .content()
                .forEach(ScheduledTransaction::terminate);
    }

}
