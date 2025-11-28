package com.jongsoft.finance.rest.api;

import static com.jongsoft.finance.rest.api.AccountMapper.toAccountLink;

import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.rest.model.*;

interface ScheduleMapper {

    static TransactionScheduleResponse toScheduleResponse(
            ScheduledTransaction scheduledTransaction) {
        var schedule = new ScheduleResponse(
                Periodicity.valueOf(
                        scheduledTransaction.getSchedule().periodicity().name()),
                scheduledTransaction.getSchedule().interval());
        var response = new TransactionScheduleResponse();
        var transferBetween = new ScheduleRequestTransferBetween(
                toAccountLink(scheduledTransaction.getSource()),
                toAccountLink(scheduledTransaction.getDestination()));

        response.id(scheduledTransaction.getId());
        response.schedule(schedule);
        response.amount(scheduledTransaction.getAmount());
        response.name(scheduledTransaction.getName());
        response.description(scheduledTransaction.getDescription());
        response.activeBetween(
                new DateRange(scheduledTransaction.getStart(), scheduledTransaction.getEnd()));
        response.transferBetween(transferBetween);

        if (scheduledTransaction.getContract() != null) {
            response.forContract(
                    ContractMapper.toContractResponse(scheduledTransaction.getContract()));
        }

        return response;
    }
}
