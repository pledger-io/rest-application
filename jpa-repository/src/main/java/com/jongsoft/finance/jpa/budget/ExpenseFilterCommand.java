package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.providers.ExpenseProvider;
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
