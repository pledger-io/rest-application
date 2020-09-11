package com.jongsoft.finance.jpa.account;

import java.util.Objects;

import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.jpa.core.FilterCommandJpa;
import com.jongsoft.lang.collection.Sequence;

import io.micronaut.data.model.Sort;

public class AccountFilterCommand extends FilterCommandJpa implements AccountProvider.FilterCommand {

    private int page;
    private int pageSize;

    public AccountFilterCommand() {
        pageSize = Integer.MAX_VALUE;
        page = 0;
    }

    @Override
    public AccountFilterCommand name(String value, boolean exact) {
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
    public AccountFilterCommand iban(String value, boolean exact) {
        if (exact) {
            hql("iban", " and lower(a.iban) = lower(:iban)");
            parameter("iban", value);
        } else {
            hql("iban", " and lower(a.iban) like lower(:iban)");
            parameter("iban", "%" + value + "%");
        }

        return this;
    }

    @Override
    public AccountFilterCommand number(String value, boolean exact) {
        if (exact) {
            hql("number", " and lower(a.number) = :number");
            parameter("number", value);
        } else {
            hql("number", " and lower(a.number) like :number");
            parameter("number", "%" + value + "%");
        }

        return this;
    }

    @Override
    public AccountFilterCommand types(Sequence<String> types) {
        if (!types.isEmpty()) {
            hql("types", " and a.type.label in (:types)");
            parameter("types", types.toJava());
        }
        return this;
    }

    @Override
    public AccountFilterCommand page(int value) {
        page = value;
        return this;
    }

    @Override
    public AccountFilterCommand pageSize(int value) {
        pageSize = value;
        return this;
    }

    public int page() {
        return page;
    }

    public int pageSize() {
        return pageSize;
    }

    @Override
    public AccountFilterCommand user(String username) {
        hql("username", " and a.user.username = :username");
        parameter("username", username);
        return this;
    }

    @Override
    public Sort sort() {
        return Sort.of(Sort.Order.asc("a.name"));
    }

    @Override
    protected String fromHql() {
        return " from AccountJpa a where a.archived = false";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AccountFilterCommand that) {
            return page == that.page &&
                    pageSize == that.pageSize &&
                    super.equals(that);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), page, pageSize);
    }

    @Override
    public String toString() {
        return generateHql();
    }

}
