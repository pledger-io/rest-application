package com.jongsoft.finance.factory;

import com.jongsoft.finance.providers.*;

public interface FilterFactory {

  AccountProvider.FilterCommand account();

  TagProvider.FilterCommand tag();

  TransactionProvider.FilterCommand transaction();

  ExpenseProvider.FilterCommand expense();

  CategoryProvider.FilterCommand category();

  TransactionScheduleProvider.FilterCommand schedule();
}
