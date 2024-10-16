package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionSuggestionResourceTest extends TestSetup {

    @Inject
    private TransactionRuleProvider ruleProvider;
    @Inject
    private AccountProvider accountProvider;

    @MockBean
    TransactionRuleProvider ruleProvider() {
        return Mockito.mock(TransactionRuleProvider.class);
    }

    @MockBean
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @Test
    void suggest(RequestSpecification requestSpecification) {
        final TransactionRule transactionRule = TransactionRule.builder()
                .id(1L)
                .name("Grocery Store 1")
                .active(true)
                .restrictive(false)
                .group("Grocery")
                .conditions(Collections.List())
                .changes(Collections.List())
                .user(ACTIVE_USER)
                .build();
        transactionRule.new Condition(1L, RuleColumn.TO_ACCOUNT, RuleOperation.CONTAINS, "Store");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.LESS_THAN, "100.00");
        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");

        Mockito.when(accountProvider.lookup(2)).thenReturn(Control.Option(Account.builder().name("Walmart Total Store").id(1L).build()));
        Mockito.when(ruleProvider.lookup()).thenReturn(Collections.List(transactionRule));

        requestSpecification.given()
                .body("""
                        {
                            "amount": 82.10,
                            "destination": "Walmart Store"
                        }""")
            .when()
                .post("/api/transactions/suggestions")
            .then()
                .statusCode(200)
                .body("TO_ACCOUNT.name", Matchers.equalTo("Walmart Total Store"));
    }
}