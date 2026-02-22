package com.jongsoft.finance.budget.domain.jpa.filter;

import com.jongsoft.finance.budget.adapter.api.ExpenseProvider;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpenseJpa;
import com.jongsoft.finance.core.domain.jpa.query.JpaFilterBuilder;

import jakarta.inject.Singleton;

@Singleton
public class ExpenseFilterCommand extends JpaFilterBuilder<ExpenseJpa>
        implements ExpenseProvider.FilterCommand {

    public ExpenseFilterCommand() {
        query().fieldEq("archived", false);
        orderBy = "name";
        orderAscending = true;
    }

    public void user(String username) {
        query().fieldEq("user.username", username);
    }

    @Override
    public ExpenseFilterCommand name(String value, boolean exact) {
        if (exact) {
            query().fieldEq("name", value);
        } else {
            query().fieldLike("name", value);
        }

        return this;
    }

    @Override
    public Class<ExpenseJpa> entityType() {
        return ExpenseJpa.class;
    }
}
