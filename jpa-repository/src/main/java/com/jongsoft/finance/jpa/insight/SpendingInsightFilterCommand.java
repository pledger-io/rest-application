package com.jongsoft.finance.jpa.insight;

import com.jongsoft.finance.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.providers.SpendingInsightProvider;
import java.time.YearMonth;

public class SpendingInsightFilterCommand extends JpaFilterBuilder<SpendingInsightJpa>
    implements SpendingInsightProvider.FilterCommand {

  public SpendingInsightFilterCommand() {
    orderAscending = true;
    orderBy = "detectedDate";
  }

  @Override
  public SpendingInsightFilterCommand category(String value, boolean exact) {
    if (exact) {
      query().fieldEq("category", value);
    } else {
      query().fieldLike("category", value);
    }
    return this;
  }

  @Override
  public SpendingInsightFilterCommand yearMonth(YearMonth yearMonth) {
    query().fieldEq("yearMonth", yearMonth.toString());
    return this;
  }

  @Override
  public SpendingInsightFilterCommand page(int page, int pageSize) {
    limitRows = pageSize;
    skipRows = page * pageSize;
    return this;
  }

  public void user(String username) {
    query().fieldEq("user.username", username);
  }

  @Override
  public Class<SpendingInsightJpa> entityType() {
    return SpendingInsightJpa.class;
  }
}
