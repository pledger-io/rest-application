package com.jongsoft.finance.jpa.tag;

import com.jongsoft.finance.jpa.core.FilterCommandJpa;
import com.jongsoft.finance.providers.TagProvider;

import java.util.Objects;

public class TagFilterCommand extends FilterCommandJpa implements TagProvider.FilterCommand {

    private int page;
    private int pageSize;

    public TagFilterCommand() {
        pageSize = Integer.MAX_VALUE;
        page = 0;
    }

    @Override
    public TagFilterCommand name(String value, boolean exact) {
        if (exact) {
            hql("name", " and lower(a.name) = lower(:name)");
            parameter("name", value);
        } else {
            hql("name", " and lower(a.name) like lower(:name)");
            parameter("name", "%" + value + "%");
        }

        return this;
    }

    @Override
    public TagFilterCommand page(int page) {
        this.page = page;
        return this;
    }

    @Override
    public TagFilterCommand pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public Sort sort() {
        return new Sort("a.name", true);
    }

    public int page() {
        return page;
    }

    public int pageSize() {
        return pageSize;
    }

    @Override
    protected String fromHql() {
        return " from TagJpa a where a.archived = false";
    }

    @Override
    public FilterCommandJpa user(String username) {
        hql("user", " and a.user.username = :username");
        parameter("username", username);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TagFilterCommand other) {
            return page == other.page &&
                    pageSize == other.pageSize
                    && super.equals(other);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), page, pageSize);
    }
}
