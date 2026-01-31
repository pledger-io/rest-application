package com.jongsoft.finance.spending.domain.jpa.filter;

import com.jongsoft.finance.core.domain.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.spending.adapter.api.SpendingPatternProvider;
import com.jongsoft.finance.spending.domain.jpa.entity.SpendingPatternJpa;

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
            query().fieldEq("category", value);
        } else {
            query().fieldLike("category", value);
        }
        return this;
    }

    @Override
    public SpendingPatternFilterCommand yearMonth(YearMonth yearMonth) {
        query().fieldEq("yearMonth", yearMonth.toString());
        return this;
    }

    @Override
    public SpendingPatternFilterCommand page(int page, int pageSize) {
        limitRows = pageSize;
        skipRows = page * pageSize;
        return this;
    }

    public void user(String username) {
        query().fieldEq("user.username", username);
    }

    @Override
    public Class<SpendingPatternJpa> entityType() {
        return SpendingPatternJpa.class;
    }
}
