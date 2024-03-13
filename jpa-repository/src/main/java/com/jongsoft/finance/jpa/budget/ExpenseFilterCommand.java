package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.jpa.core.FilterCommandJpa;
import com.jongsoft.finance.providers.ExpenseProvider;
import jakarta.inject.Singleton;

@Singleton
public class ExpenseFilterCommand extends FilterCommandJpa implements ExpenseProvider.FilterCommand {

    @Override
    protected String fromHql() {
        return " from ExpenseJpa a where a.archived = false";
    }

    @Override
    public FilterCommandJpa user(String username) {
        hql("user", " and a.user.username = :username");
        parameter("username", username);
        return this;
    }

    @Override
    public ExpenseFilterCommand name(String value, boolean exact) {
        if (exact) {
            hql("name", " and lower(a.name) = lower(:name)");
            parameter("name", value);
        } else {
            hql("name", " and lower(a.name) like lower(:name)");
            parameter("name", "%" + value + "%");
        }

        return this;
    }

    @Override
    public Sort sort() {
        return new Sort("a.name", true);
    }

    @Override
    public int page() {
        return 0;
    }

    @Override
    public int pageSize() {
        return Integer.MAX_VALUE;
    }

}
