package com.jongsoft.finance.factory;

import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.ExpenseProvider;

public interface FilterFactory {

    AccountProvider.FilterCommand account();

    TagProvider.FilterCommand tag();

    TransactionProvider.FilterCommand transaction();

    ExpenseProvider.FilterCommand expense();

    CategoryProvider.FilterCommand category();

}
