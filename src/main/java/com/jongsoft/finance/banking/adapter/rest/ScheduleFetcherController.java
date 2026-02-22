package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.rest.ScheduleFetcherApi;
import com.jongsoft.finance.rest.model.TransactionScheduleResponse;
import com.jongsoft.lang.Collections;

import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;

@Controller
class ScheduleFetcherController implements ScheduleFetcherApi {

    private final Logger logger;
    private final TransactionScheduleProvider scheduleProvider;
    private final FilterProvider<TransactionScheduleProvider.FilterCommand> filterFactory;

    public ScheduleFetcherController(
            TransactionScheduleProvider scheduleProvider,
            FilterProvider<TransactionScheduleProvider.FilterCommand> filterFactory) {
        this.scheduleProvider = scheduleProvider;
        this.filterFactory = filterFactory;
        this.logger = org.slf4j.LoggerFactory.getLogger(ScheduleFetcherController.class);
    }

    @Override
    public List<@Valid TransactionScheduleResponse> findScheduleByFilter(
            List<@NotNull Integer> account, List<@NotNull Integer> contract) {
        logger.info("Fetching transaction schedule by filters.");

        var filter = filterFactory.create().activeOnly();

        if (account != null && !account.isEmpty()) {
            filter.account(Collections.List(account).map(id -> new EntityRef((long) id)));
        }
        if (contract != null && !contract.isEmpty()) {
            filter.contract(Collections.List(contract).map(id -> new EntityRef((long) id)));
        }

        return scheduleProvider
                .lookup(filter)
                .content()
                .map(ScheduleMapper::toScheduleResponse)
                .toJava();
    }

    @Override
    public TransactionScheduleResponse findScheduleById(Long id) {
        logger.info("Fetching transaction schedule {}.", id);
        var schedule = scheduleProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("The schedule cannot be found."));
        if (schedule.getEnd() != null && !schedule.getEnd().isAfter(LocalDate.now())) {
            throw StatusException.gone("The schedule has already ended.");
        }

        return ScheduleMapper.toScheduleResponse(schedule);
    }
}
