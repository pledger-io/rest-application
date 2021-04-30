package com.jongsoft.finance.jpa.category;

import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.jpa.core.FilterCommandJpa;
import io.micronaut.data.model.Sort;

import javax.inject.Singleton;

@Singleton
public class CategoryFilterCommand extends FilterCommandJpa implements CategoryProvider.FilterCommand {
    
    private int page;
    private int pageSize;

    public CategoryFilterCommand() {
        this.page = 0;
        this.pageSize = Integer.MAX_VALUE;
    }

    @Override
    public CategoryProvider.FilterCommand label(String label, boolean exact) {
        if (exact) {
            hql("label", " and lower(a.label) = lower(:label)");
            parameter("label", label);
        } else {
            hql("label", " and lower(a.label) like lower(:label)");
            parameter("label", "%" + label + "%");
        }
        return this;
    }

    @Override
    public CategoryProvider.FilterCommand page(int page) {
        this.page = page;
        return this;
    }

    @Override
    public CategoryProvider.FilterCommand pageSize(int pageSize) {
        assert pageSize > 1;

        this.pageSize = pageSize;
        return this;
    }

    @Override
    protected String fromHql() {
        return " from CategoryJpa a where a.archived = false";
    }

    @Override
    public FilterCommandJpa user(String username) {
        hql("user", " and a.user.username = :username");
        parameter("username", username);
        return this;
    }

    @Override
    public Sort sort() {
        return Sort.of(Sort.Order.asc("a.label"));
    }

    public int page() {
        return this.page;
    }

    public int pageSize() {
        return this.pageSize;
    }
    
}
