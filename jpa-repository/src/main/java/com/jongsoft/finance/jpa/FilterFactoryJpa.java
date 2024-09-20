package com.jongsoft.finance.jpa;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.jpa.account.AccountFilterCommand;
import com.jongsoft.finance.jpa.budget.ExpenseFilterCommand;
import com.jongsoft.finance.jpa.category.CategoryFilterCommand;
import com.jongsoft.finance.jpa.schedule.ScheduleFilterCommand;
import com.jongsoft.finance.jpa.tag.TagFilterCommand;
import com.jongsoft.finance.jpa.transaction.TransactionFilterCommand;
import com.jongsoft.finance.providers.*;
import jakarta.inject.Singleton;

@Singleton
@RequiresJpa
public class FilterFactoryJpa implements FilterFactory {

    @Override
    public AccountFilterCommand account() {
        return new AccountFilterCommand();
    }

    @Override
    public TagProvider.FilterCommand tag() {
        return new TagFilterCommand();
    }

    @Override
    public TransactionProvider.FilterCommand transaction() {
        return new TransactionFilterCommand();
    }

    @Override
    public ExpenseProvider.FilterCommand expense() {
        return new ExpenseFilterCommand();
    }

    @Override
    public CategoryProvider.FilterCommand category() {
        return new CategoryFilterCommand();
    }

    @Override
    public TransactionScheduleProvider.FilterCommand schedule() {
        return new ScheduleFilterCommand();
    }
}
