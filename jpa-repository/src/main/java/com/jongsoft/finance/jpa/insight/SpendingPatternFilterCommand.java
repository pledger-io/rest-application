package com.jongsoft.finance.jpa.insight;

import static com.jongsoft.finance.jpa.insight.SpendingPatternJpa.*;

import com.jongsoft.finance.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.providers.SpendingPatternProvider;
import java.time.YearMonth;

public class SpendingPatternFilterCommand extends JpaFilterBuilder<SpendingPatternJpa>
    implements SpendingPatternProvider.FilterCommand {

  public SpendingPatternFilterCommand() {
    orderAscending = true;
    orderBy = "detectedDate";
  }

  @Override
  public SpendingPatternFilterCommand category(String value, boolean exact) {
    if (exact) {
      query().fieldEq(COLUMN_CATEGORY, value);
    } else {
      query().fieldLike(COLUMN_CATEGORY, value);
    }
    return this;
  }

  @Override
  public SpendingPatternFilterCommand yearMonth(YearMonth yearMonth) {
    query().fieldEq(COLUMN_YEAR_MONTH, yearMonth.toString());
    return this;
  }

  @Override
  public SpendingPatternFilterCommand page(int page, int pageSize) {
    limitRows = pageSize;
    skipRows = page * pageSize;
    return this;
  }

  public void user(String username) {
    query().fieldEq(COLUMN_USERNAME, username);
  }

  @Override
  public Class<SpendingPatternJpa> entityType() {
    return SpendingPatternJpa.class;
  }
}
