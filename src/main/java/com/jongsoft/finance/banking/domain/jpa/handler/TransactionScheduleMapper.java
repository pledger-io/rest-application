package com.jongsoft.finance.banking.domain.jpa.handler;

import com.jongsoft.finance.banking.domain.jpa.entity.TransactionScheduleJpa;
import com.jongsoft.finance.banking.domain.model.ScheduleValue;
import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.core.value.Schedule;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public abstract class TransactionScheduleMapper {

    @Mapper.Mapping(to = "schedule", from = "#{this.createSchedule(entity)}")
    public abstract TransactionSchedule toModel(TransactionScheduleJpa entity);

    public Schedule createSchedule(TransactionScheduleJpa entity) {
        return ScheduleValue.of(entity.getPeriodicity(), entity.getInterval());
    }
}
