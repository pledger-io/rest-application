package com.jongsoft.finance.domain;

import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.domain.user.ExpenseProvider;

public interface FilterFactory {

    AccountProvider.FilterCommand account();

    TagProvider.FilterCommand tag();

    TransactionProvider.FilterCommand transaction();

    ExpenseProvider.FilterCommand expense();

    CategoryProvider.FilterCommand category();

}
