package com.jongsoft.finance.jpa;

import javax.inject.Singleton;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.jpa.account.AccountFilterCommand;
import com.jongsoft.finance.jpa.tag.TagFilterCommand;
import com.jongsoft.finance.jpa.transaction.TransactionFilterCommand;
import com.jongsoft.finance.jpa.category.CategoryFilterCommand;
import com.jongsoft.finance.jpa.budget.ExpenseFilterCommand;

@Singleton
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

}
