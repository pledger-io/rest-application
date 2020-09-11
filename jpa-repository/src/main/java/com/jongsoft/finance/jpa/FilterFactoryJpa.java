package com.jongsoft.finance.jpa;

import javax.inject.Singleton;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.domain.user.ExpenseProvider;
import com.jongsoft.finance.jpa.account.AccountFilterCommand;
import com.jongsoft.finance.jpa.transaction.TagFilterCommand;
import com.jongsoft.finance.jpa.transaction.TransactionFilterCommand;
import com.jongsoft.finance.jpa.user.CategoryFilterCommand;
import com.jongsoft.finance.jpa.user.ExpenseFilterCommand;

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
