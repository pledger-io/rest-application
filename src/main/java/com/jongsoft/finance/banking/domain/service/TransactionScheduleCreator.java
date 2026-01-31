package com.jongsoft.finance.banking.domain.service;

import com.jongsoft.finance.banking.adapter.api.TransactionCreationHandler;
import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.domain.commands.CreateTransactionCommand;
import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.banking.types.TransactionType;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.core.domain.model.UserAccount;

import io.micronaut.scheduling.annotation.Scheduled;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Singleton
class TransactionScheduleCreator {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(TransactionScheduleCreator.class);

    private final UserProvider userProvider;
    private final TransactionScheduleProvider transactionScheduleProvider;
    private final TransactionCreationHandler transactionCreationHandler;

    TransactionScheduleCreator(
            UserProvider userProvider,
            TransactionScheduleProvider transactionScheduleProvider,
            TransactionCreationHandler transactionCreationHandler) {
        this.userProvider = userProvider;
        this.transactionScheduleProvider = transactionScheduleProvider;
        this.transactionCreationHandler = transactionCreationHandler;
    }

    @Scheduled(cron = "${application.schedules.banking.scheduled-transactions.cron}")
    void createScheduledTransactions() {
        for (UserAccount userAccount : userProvider.lookup()) {
            MDC.put("correlationId", UUID.randomUUID().toString());
            InternalAuthenticationEvent.authenticate(userAccount.getUsername().email());

            log.info(
                    "Checking for {} if there are transactions to create.",
                    userAccount.getUsername().email());
            transactionScheduleProvider.lookup().stream()
                    .filter(TransactionSchedule::shouldCreateTransaction)
                    .forEach(this::createTransactionForSchedule);
        }
    }

    void createTransactionForSchedule(TransactionSchedule schedule) {
        log.info("Creating transaction for schedule {}.", schedule.getId());
        var command = new CreateTransactionCommand(
                LocalDate.now(),
                schedule.getDescription(),
                TransactionType.CREDIT,
                null,
                schedule.getSource().getCurrency(),
                schedule.getSource().getId(),
                schedule.getDestination().getId(),
                BigDecimal.valueOf(schedule.getAmount()));

        long id = transactionCreationHandler.handleCreatedEvent(command);

        schedule.reschedule();
    }
}
