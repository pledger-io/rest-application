package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.jpa.query.JpaFilterBuilder;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.lang.collection.Sequence;

public class AccountFilterCommand extends JpaFilterBuilder<AccountJpa> implements AccountProvider.FilterCommand {

    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_IBAN = "iban";

    public AccountFilterCommand() {
        orderAscending = true;
        orderBy = FIELD_NAME;
    }

    @Override
    public AccountFilterCommand name(String value, boolean exact) {
        if (exact) {
            query().fieldEq(FIELD_NAME, value.toLowerCase());
        } else {
            query().fieldLike(FIELD_NAME, value.toLowerCase());
        }

        return this;
    }

    @Override
    public AccountFilterCommand iban(String value, boolean exact) {
        if (exact) {
            query().fieldEq(FIELD_IBAN, value.toLowerCase());
        } else {
            query().fieldLike(FIELD_IBAN, value.toLowerCase());
        }

        return this;
    }

    @Override
    public AccountFilterCommand number(String value, boolean exact) {
        if (exact) {
            query().fieldEq(FIELD_NUMBER, value.toLowerCase());
        } else {
            query().fieldLike(FIELD_NUMBER, value.toLowerCase());
        }

        return this;
    }

    @Override
    public AccountFilterCommand types(Sequence<String> types) {
        if (!types.isEmpty()) {
            query().fieldEqOneOf("type.label", types.stream().toArray());
        }
        return this;
    }

    @Override
    public AccountFilterCommand page(int page, int pageSize) {
        skipRows = page * pageSize;
        limitRows = pageSize;
        return this;
    }

    public void user(String username) {
        query().fieldEq("user.username", username);
    }

    @Override
    public Class<AccountJpa> entityType() {
        return AccountJpa.class;
    }
}
