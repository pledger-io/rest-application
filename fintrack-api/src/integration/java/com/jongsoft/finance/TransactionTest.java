package com.jongsoft.finance;

import com.jongsoft.finance.extension.AccountContext;
import com.jongsoft.finance.extension.IntegrationTest;
import com.jongsoft.finance.extension.TestContext;
import com.jongsoft.finance.extension.TransactionContext;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;

@IntegrationTest(phase = 2)
@DisplayName("User creates the initial transactions:")
public class TransactionTest {

    @Test
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
                        LocalDate.parse("2020-01-01"))
                .list(LocalDate.parse("2020-01-01"), response -> response
                        .body("info.records", equalTo(1))
                        .body("content.amount", hasItem(1000.0f)));
    }

    @Test
    @DisplayName("Buy groceries several times")
    void spendMoney(TestContext context) {
        context.authenticate("sample@e", "Zomer2020");

        var checkingAccount = new MutableObject<AccountContext.Account>();
        var groceryStore = new MutableObject<AccountContext.Account>();

        context.accounts()
                .locate("My checking account", checkingAccount::setValue)
                .locate("Groceries are us", groceryStore::setValue);

        context.transactions()
                .create(
                        checkingAccount.getValue(),
                        groceryStore.getValue(),
                        22.32,
                        "Groceries",
                        LocalDate.parse("2020-01-02"))
                .create(
                        checkingAccount.getValue(),
                        groceryStore.getValue(),
                        15.00,
                        "Groceries",
                        LocalDate.parse("2020-01-03"))
                .create(
                        checkingAccount.getValue(),
                        groceryStore.getValue(),
                        10.00,
                        "Groceries",
                        LocalDate.parse("2020-02-04"))
                .list(LocalDate.parse("2020-01-02"), response -> response
                        .body("info.records", equalTo(1))
                        .body("content.amount", hasItem(22.32f)))
                .list(LocalDate.parse("2020-01-03"), response -> response
                        .body("info.records", equalTo(1))
                        .body("content.amount", hasItem(15f)));
    }

    @Test
    void splitTransaction(TestContext context) {
      context.authenticate("sample@e", "Zomer2020");

      var checkingAccount = new MutableObject<AccountContext.Account>();
      var shop = new MutableObject<AccountContext.Account>();
      var transactionId = new MutableLong();

      context.accounts()
          .locate("My checking account", checkingAccount::setValue)
          .locate("Guarda", shop::setValue);

      context.transactions()
          .create(
              checkingAccount.getValue(),
              shop.getValue(),
              25,
              "Total spending habit",
              LocalDate.parse("2023-01-01"))
          .list(LocalDate.parse("2023-01-01"), response ->
              transactionId.setValue(response.extract().jsonPath().getLong("content[0].id")))
          .split(shop.getValue().getId(), transactionId.getValue(), List.of(
              new TransactionContext.TransactionSplit("First part spending", 10),
              new TransactionContext.TransactionSplit("Second part spending", 15)))
          .list(LocalDate.parse("2023-01-01"), response -> response
              .body("info.records", equalTo(1))
              .body("content.amount", hasItem(25f))
              .body("content[0].split.amount", hasItems(15f, 10f)));
    }
}
