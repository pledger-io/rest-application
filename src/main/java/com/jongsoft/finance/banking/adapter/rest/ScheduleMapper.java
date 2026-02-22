package com.jongsoft.finance.banking.adapter.rest;

import static com.jongsoft.finance.banking.adapter.rest.AccountMapper.toAccountLink;

import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.rest.model.*;

interface ScheduleMapper {

    static TransactionScheduleResponse toScheduleResponse(
            TransactionSchedule scheduledTransaction) {
        var schedule = new ScheduleResponse(
                Periodicity.valueOf(
                        scheduledTransaction.getSchedule().periodicity().name()),
                scheduledTransaction.getSchedule().interval());
        var response = new TransactionScheduleResponse();
        var transferBetween = new TransactionScheduleRequestTransferBetween(
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

        return response;
    }
}
