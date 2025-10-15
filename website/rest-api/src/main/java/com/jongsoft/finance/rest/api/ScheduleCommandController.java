package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.rest.model.SchedulePatchRequest;
import com.jongsoft.finance.rest.model.ScheduleRequest;
import com.jongsoft.finance.rest.model.TransactionScheduleResponse;
import com.jongsoft.finance.schedule.Periodicity;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;

import java.time.LocalDate;

@Controller
class ScheduleCommandController implements ScheduleCommandApi {

    private final Logger logger;
    private final AccountProvider accountProvider;
    private final TransactionScheduleProvider scheduleProvider;

    ScheduleCommandController(
            AccountProvider accountProvider, TransactionScheduleProvider scheduleProvider) {
        this.accountProvider = accountProvider;
        this.scheduleProvider = scheduleProvider;
        this.logger = org.slf4j.LoggerFactory.getLogger(ScheduleCommandController.class);
    }

    @Override
    public HttpResponse<@Valid TransactionScheduleResponse> createSchedule(
            ScheduleRequest scheduleRequest) {
        logger.info("Creating new transaction schedule {}.", scheduleRequest.getName());

        var source = accountProvider
                .lookup(scheduleRequest.getTransferBetween().getSource().getId())
                .getOrThrow(() -> StatusException.badRequest(
                        "The source account cannot be found.", "contract.source.not.found"));
        var destination = accountProvider
                .lookup(scheduleRequest.getTransferBetween().getDestination().getId())
                .getOrThrow(() -> StatusException.badRequest(
                        "The destination account cannot be found.",
                        "contract.destination.not.found"));

        source.createSchedule(
                scheduleRequest.getName(),
                new ScheduleValue(
                        Periodicity.valueOf(
                                scheduleRequest.getSchedule().getPeriodicity().name()),
                        scheduleRequest.getSchedule().getInterval()),
                destination,
                scheduleRequest.getAmount());

        var schedule = scheduleProvider
                .lookup()
                .first(s -> s.getName().equals(scheduleRequest.getName()))
                .getOrThrow(
                        () -> StatusException.internalError("Could not locate created schedule"));
        return HttpResponse.created(ScheduleMapper.toScheduleResponse(schedule));
    }

    @Override
    public HttpResponse<Void> deleteSchedule(Long id) {
        logger.info("Deleting transaction schedule {}.", id);

        var schedule = lookupScheduledTransactionOrThrow(id);
        schedule.terminate();
        return HttpResponse.noContent();
    }

    @Override
    public TransactionScheduleResponse updateSchedule(
            Long id, SchedulePatchRequest schedulePatchRequest) {
        logger.info("Updating transaction schedule {}.", id);
        var schedule = lookupScheduledTransactionOrThrow(id);

        if (schedulePatchRequest.getName() != null) {
            schedule.describe(
                    schedulePatchRequest.getName(), schedulePatchRequest.getDescription());
        }

        if (schedulePatchRequest.getSchedule() != null) {
            schedule.adjustSchedule(
                    Periodicity.valueOf(
                            schedulePatchRequest.getSchedule().getPeriodicity().name()),
                    schedulePatchRequest.getSchedule().getInterval());
        }

        if (schedulePatchRequest.getActiveBetween() != null) {
            schedule.limit(
                    schedulePatchRequest.getActiveBetween().getStartDate(),
                    schedulePatchRequest.getActiveBetween().getEndDate());
        }

        return ScheduleMapper.toScheduleResponse(schedule);
    }

    private ScheduledTransaction lookupScheduledTransactionOrThrow(Long id) {
        var schedule = scheduleProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("The schedule cannot be found."));
        if (schedule.getEnd() != null && schedule.getEnd().isBefore(LocalDate.now())) {
            throw StatusException.gone("The schedule has already ended.");
        }

        return schedule;
    }
}
