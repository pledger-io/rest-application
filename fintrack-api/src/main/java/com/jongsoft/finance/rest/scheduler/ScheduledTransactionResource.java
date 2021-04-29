package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.rest.model.ScheduledTransactionResponse;
import com.jongsoft.lang.Control;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Objects;

@Tag(name = "Transactions")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/schedule/transaction")
public class ScheduledTransactionResource {

    private final AccountProvider accountProvider;
    private final TransactionScheduleProvider scheduleProvider;

    public ScheduledTransactionResource(AccountProvider accountProvider, TransactionScheduleProvider scheduleProvider) {
        this.accountProvider = accountProvider;
        this.scheduleProvider = scheduleProvider;
    }

    @Get
    @Operation(
            operationId = "listTransactionSchedule",
            summary = "List all available transaction schedules"
    )
    public Flowable<ScheduledTransactionResponse> list() {
        return Flowable.create(emitter -> {
            scheduleProvider.lookup()
                    .map(ScheduledTransactionResponse::new)
                    .forEach(emitter::onNext);
            emitter.onComplete();
        }, BackpressureStrategy.LATEST);
    }

    @Put
    @Operation(
            operationId = "createTransactionSchedule",
            summary = "Create a new transaction schedule"
    )
    public Single<HttpResponse<ScheduledTransactionResponse>> create(@Valid @Body ScheduledTransactionCreateRequest request) {
        return Single.create(emitter -> {
            var account = accountProvider.lookup(request.getSource().getId());
            var destination = accountProvider.lookup(request.getDestination().getId());

            if (!account.isPresent() || !destination.isPresent()) {
                emitter.onError(new IllegalArgumentException("Either source or destination account cannot be located."));
            } else {
                account.get().createSchedule(
                        request.getName(),
                        request.getSchedule(),
                        destination.get(),
                        request.getAmount());

                emitter.onSuccess(HttpResponse.created(scheduleProvider.lookup()
                        .filter(schedule -> request.getName().equals(schedule.getName()))
                        .map(ScheduledTransactionResponse::new)
                        .head()));
            }
        });
    }

    @Get("/{scheduleId}")
    @Operation(
            operationId = "fetchTransactionSchedule",
            summary = "Get a single transaction schedule",
            description = "Lookup a transaction schedule in the system by its technical id",
            parameters = @Parameter(
                    name = "scheduleId",
                    description = "The technical id of the transaction schedule",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH)
    )
    public Single<HttpResponse<ScheduledTransactionResponse>> get(@PathVariable long scheduleId) {
        return Single.create(emitter -> {
            var scheduleOption = scheduleProvider.lookup()
                    .filter(s -> s.getId() == scheduleId)
                    .map(ScheduledTransactionResponse::new);
            if (scheduleOption.isEmpty()) {
                emitter.onSuccess(HttpResponse.notFound());
            } else {
                emitter.onSuccess(HttpResponse.ok(scheduleOption.head()));
            }
        });
    }

    @Patch("/{scheduleId}")
    @Operation(
            operationId = "patchTransactionSchedule",
            summary = "Update part of a transaction schedule",
            parameters = @Parameter(
                    name = "scheduleId",
                    description = "The technical id of the transaction schedule",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH)
    )
    public Single<HttpResponse<ScheduledTransactionResponse>> patch(
            @PathVariable long scheduleId,
            @Valid @Body ScheduledTransactionPatchRequest request) {
        return Single.create(emitter -> {
            var scheduleOption = scheduleProvider.lookup()
                    .filter(s -> s.getId() == scheduleId);
            if (scheduleOption.isEmpty()) {
                emitter.onSuccess(HttpResponse.notFound());
            } else {
                var schedule = scheduleOption.head();
                if (Objects.nonNull(request.getName())) {
                    schedule.describe(request.getName(), request.getDescription());
                }

                if (Objects.nonNull(request.getSchedule())) {
                    schedule.adjustSchedule(request.getSchedule().periodicity(), request.getSchedule().interval());
                }

                if (Objects.nonNull(request.getRange())) {
                    schedule.limit(request.getRange().getStart(), request.getRange().getEnd());
                }

                emitter.onSuccess(HttpResponse.ok(new ScheduledTransactionResponse(schedule)));
            }
        });
    }

    @Delete("/{scheduleId}")
    @Operation(
            operationId = "removeTransactionSchedule",
            summary = "Remove a transaction schedule",
            parameters = @Parameter(
                    name = "scheduleId",
                    description = "The technical id of the transaction schedule",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH)
    )
    public HttpResponse<Void> remove(@PathVariable long scheduleId) {
        var toRemove = scheduleProvider.lookup()
                .filter(schedule -> schedule.getId() == scheduleId);

        if (toRemove.isEmpty()) {
            return HttpResponse.notFound();
        } else {
            var schedule = toRemove.get();
            schedule.terminate();
            return HttpResponse.ok();
        }
    }

}
