package com.jongsoft.finance.jpa.category;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.providers.CategoryProvider;

import jakarta.inject.Singleton;

@Singleton
@RequiresJpa
public class CategoryFilterCommand extends JpaFilterBuilder<CategoryJpa>
        implements CategoryProvider.FilterCommand {

    public CategoryFilterCommand() {
        query().fieldEq("archived", false);
        orderAscending = true;
        orderBy = "label";
    }

    @Override
    public CategoryProvider.FilterCommand label(String label, boolean exact) {
        if (exact) {
            query().fieldEq("label", label);
        } else {
            query().fieldLike("label", label);
        }
        return this;
    }

    @Override
    public CategoryProvider.FilterCommand page(int page, int pageSize) {
        limitRows = pageSize;
        skipRows = pageSize * page;
        return this;
    }

    public void user(String username) {
        query().fieldEq("user.username", username);
    }

    @Override
    public Class<CategoryJpa> entityType() {
        return CategoryJpa.class;
    }
}
