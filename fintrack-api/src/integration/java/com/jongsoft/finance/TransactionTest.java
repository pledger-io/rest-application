package com.jongsoft.finance;

import com.jongsoft.finance.extension.AccountContext;
import com.jongsoft.finance.extension.IntegrationTest;
import com.jongsoft.finance.extension.TestContext;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@IntegrationTest(phase = 2)
@DisplayName("User creates the initial transactions:")
public class TransactionTest {

    @Test
    @Order(1)
    @DisplayName("Book the first salary income")
    void firstIncome(TestContext context) {
        context.authenticate("sample@e", "Zomer2020");

        var checkingAccount = new MutableObject<AccountContext.Account>();
        var employer = new MutableObject<AccountContext.Account>();

        context.accounts()
                .locate("My checking account", checkingAccount::setValue)
                .locate("Chicko Cointer", employer::setValue);

        context.transactions()
                .create(
                        checkingAccount.getValue(),
                        employer.getValue(),
                        1000,
                        "First income",
                        LocalDate.parse("2020-01-01"));
    }
}
