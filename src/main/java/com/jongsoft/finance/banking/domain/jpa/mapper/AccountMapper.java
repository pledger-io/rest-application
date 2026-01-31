package com.jongsoft.finance.banking.domain.jpa.mapper;

import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.SavingGoalJpa;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.SavingGoal;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public abstract class AccountMapper {

    public Set<SavingGoal> mapSavingGoals(AccountJpa entity) {
        return entity.getSavingGoals().stream().map(this::toDomain).collect(Collectors.toSet());
    }

    @Mapper.Mapping(to = "type", from = "#{entity.type.label}")
    @Mapper.Mapping(to = "user", from = "#{entity.user.username}")
    @Mapper.Mapping(to = "remove", from = "#{entity.archived}")
    @Mapper.Mapping(to = "currency", from = "#{entity.currency.code}")
    @Mapper.Mapping(to = "savingGoals", from = "#{this.mapSavingGoals(entity)}")
    public abstract Account toDomain(AccountJpa entity);

    @Mapper.Mapping(to = "account", from = "#{null}")
    @Mapper.Mapping(
            to = "schedule",
            from =
                    "#{goal.periodicity != null ? T(com.jongsoft.finance.banking.domain.model.ScheduleValue).of(goal.periodicity, goal.interval): null}")
    public abstract SavingGoal toDomain(SavingGoalJpa goal);
}
