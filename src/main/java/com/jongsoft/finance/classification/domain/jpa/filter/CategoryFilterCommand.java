package com.jongsoft.finance.classification.domain.jpa.filter;

import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.classification.domain.jpa.entity.CategoryJpa;
import com.jongsoft.finance.core.domain.jpa.query.JpaFilterBuilder;

import jakarta.inject.Singleton;

@Singleton
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
