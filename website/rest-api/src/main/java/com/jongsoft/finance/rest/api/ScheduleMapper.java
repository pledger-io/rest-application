package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.rest.model.DateRange;
import com.jongsoft.finance.rest.model.Periodicity;
import com.jongsoft.finance.rest.model.ScheduleResponse;
import com.jongsoft.finance.rest.model.TransactionScheduleResponse;

interface ScheduleMapper {

    static TransactionScheduleResponse toScheduleResponse(
            ScheduledTransaction scheduledTransaction) {
        var schedule = new ScheduleResponse(
                Periodicity.valueOf(
                        scheduledTransaction.getSchedule().periodicity().name()),
                scheduledTransaction.getSchedule().interval());
        var response = new TransactionScheduleResponse();

        response.id(scheduledTransaction.getId());
        response.schedule(schedule);
        response.amount(scheduledTransaction.getAmount());
        response.name(scheduledTransaction.getName());
        response.description(scheduledTransaction.getDescription());
        response.activeBetween(
                new DateRange(scheduledTransaction.getStart(), scheduledTransaction.getEnd()));

        if (scheduledTransaction.getContract() != null) {
            response.forContract(
                    ContractMapper.toContractResponse(scheduledTransaction.getContract()));
        }

        return response;
    }
}
