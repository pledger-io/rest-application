package com.jongsoft.finance.budget.domain.jpa.handler;

import com.jongsoft.finance.budget.domain.commands.CloseBudgetCommand;
import com.jongsoft.finance.budget.domain.commands.CreateBudgetCommand;
import com.jongsoft.finance.budget.domain.jpa.entity.BudgetJpa;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpenseJpa;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpensePeriodJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.math.BigDecimal;

@Singleton
@Transactional
class BudgetChangeHandler {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(BudgetChangeHandler.class);
    private final ReactiveEntityManager entityManager;

    BudgetChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleCreate(CreateBudgetCommand command) {
        log.info("[{}] - Processing budget create event", command.start());

        BudgetJpa budget = new BudgetJpa(
                command.expectedIncome(), command.start(), entityManager.currentUser());

        entityManager.persist(budget);

        for (var expense : command.expenses()) {
            ExpenseJpa expenseJpa = entityManager.getById(ExpenseJpa.class, expense.expenseId());
            ExpensePeriodJpa expensePeriodJpa = new ExpensePeriodJpa(
                    new BigDecimal(expense.expected()).subtract(new BigDecimal("0.01")),
                    new BigDecimal(expense.expected()),
                    expenseJpa,
                    budget);

            entityManager.persist(expensePeriodJpa);
            budget.getExpenses().add(expensePeriodJpa);
        }
    }

    @EventListener
    public void handleClose(CloseBudgetCommand command) {
        log.info("[{}] - Processing budget closing event", command.id());

        entityManager
                .update(BudgetJpa.class)
                .set("until", command.end())
                .fieldEq("id", command.id())
                .execute();
    }
}
