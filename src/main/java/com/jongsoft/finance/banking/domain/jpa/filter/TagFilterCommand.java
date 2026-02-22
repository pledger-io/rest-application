package com.jongsoft.finance.banking.domain.jpa.filter;

import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.banking.domain.jpa.entity.TagJpa;
import com.jongsoft.finance.core.domain.jpa.query.JpaFilterBuilder;

public class TagFilterCommand extends JpaFilterBuilder<TagJpa>
        implements TagProvider.FilterCommand {

    public TagFilterCommand() {
        query().fieldEq("archived", false);
        orderAscending = true;
        orderBy = "name";
    }

    @Override
    public TagFilterCommand name(String value, boolean exact) {
        if (exact) {
            query().fieldEq("name", value);
        } else {
            query().fieldLike("name", value);
        }

        return this;
    }

    @Override
    public TagFilterCommand page(int page, int pageSize) {
        limitRows = pageSize;
        skipRows = page * pageSize;
        return this;
    }

    @Override
    public void user(String username) {
        query().fieldEq("user.username", username);
    }

    @Override
    public Class<TagJpa> entityType() {
        return TagJpa.class;
    }
}
