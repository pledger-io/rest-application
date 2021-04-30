package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.jpa.core.FilterCommandJpa;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.data.model.Sort;

import java.util.Objects;

public class AccountFilterCommand extends FilterCommandJpa implements AccountProvider.FilterCommand {

    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_IBAN = "iban";

    private int page;
    private int pageSize;

    public AccountFilterCommand() {
        pageSize = Integer.MAX_VALUE;
        page = 0;
    }

    @Override
    public AccountFilterCommand name(String value, boolean exact) {
        if (exact) {
            hql(FIELD_NAME, " and lower(a.name) = lower(:name)");
            parameter(FIELD_NAME, value);
        } else {
            hql(FIELD_NAME, " and lower(a.name) like lower(:name)");
            parameter(FIELD_NAME, "%" + value + "%");
        }

        return this;
    }

    @Override
    public AccountFilterCommand iban(String value, boolean exact) {
        if (exact) {
            hql(FIELD_IBAN, " and lower(a.iban) = lower(:iban)");
            parameter(FIELD_IBAN, value);
        } else {
            hql(FIELD_IBAN, " and lower(a.iban) like lower(:iban)");
            parameter(FIELD_IBAN, "%" + value + "%");
        }

        return this;
    }

    @Override
    public AccountFilterCommand number(String value, boolean exact) {
        if (exact) {
            hql(FIELD_NUMBER, " and lower(a.number) = :number");
            parameter(FIELD_NUMBER, value);
        } else {
            hql(FIELD_NUMBER, " and lower(a.number) like :number");
            parameter(FIELD_NUMBER, "%" + value + "%");
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
