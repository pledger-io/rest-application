package com.jongsoft.finance.budget.domain.jpa.mapper;

import com.jongsoft.finance.budget.domain.jpa.entity.BudgetJpa;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpensePeriodJpa;
import com.jongsoft.finance.budget.domain.model.Budget;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

import java.util.List;
import java.util.Set;

@Singleton
public interface BudgetMapper {

    @Mapper.Mapping(to = "start", from = "#{entity.from}")
    @Mapper.Mapping(to = "end", from = "#{entity.until}")
    @Mapper.Mapping(to = "expenses", from = "#{this.expensesList(entity.expenses)}")
    Budget toDomain(BudgetJpa entity);

    @Mapper.Mapping(to = "id", from = "#{entity.expense.id}")
    @Mapper.Mapping(to = "name", from = "#{entity.expense.name}")
    @Mapper.Mapping(to = "upperBound", from = "#{entity.upperBound}")
    Budget.Expense toDomain(ExpensePeriodJpa entity);

    default List<Budget.Expense> expensesList(Set<ExpensePeriodJpa> entity) {
        return entity.stream().map(this::toDomain).toList();
    }
}
