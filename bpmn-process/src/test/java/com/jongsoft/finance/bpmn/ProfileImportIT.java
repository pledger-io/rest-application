package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.bpmn.process.ProcessExtension;
import com.jongsoft.finance.bpmn.process.RuntimeContext;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.lang.collection.Sequence;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Map;

@ProcessExtension
@DisplayName("Profile import feature")
public class ProfileImportIT {

    @Test
    @DisplayName("Import a profile with accounts")
    void runWithAccounts(RuntimeContext context) {
        context
            .withStorage()
            .withStorage("my-sample-token", "/profile-test/accounts-only.json");

        var process = context.execute("ImportUserProfile", Map.of(
                "storageToken", "my-sample-token"
        ));

        process.verifyCompleted();

        context
            .verifyAccountCreated("Demo checking account", "EUR", "default")
            .verifyAccountCreated("Groceries are us", "EUR", "creditor")
            .verifyAccountCreated("Boss & Co.", "EUR", "debtor");
    }

    @Test
    @DisplayName("Import a profile with rules")
    void runWithRules(RuntimeContext context) {
        context
            .withStorage()
            .withStorage("my-sample-token", "/profile-test/rules-only.json")
            .withCategory("Salary")
            .withCategory("Groceries")
            .withAccount(Account.builder()
                    .id(1L)
                    .name("Groceries are us")
                    .currency("EUR")
                    .type("creditor")
                    .build());

        var process = context.execute("ImportUserProfile", Map.of(
                "storageToken", "my-sample-token"
        ));

        process.verifyCompleted();

        context
            .verifyRuleCreated("Groceries matcher", rule -> rule.extracting("conditions")
                    .isInstanceOf(Sequence.class)
                    .satisfies(conditions -> Assertions.assertThat((Sequence<?>)conditions)
                            .hasSize(3)
                            .extracting("field")
                            .containsExactly(RuleColumn.DESCRIPTION, RuleColumn.AMOUNT, RuleColumn.AMOUNT)));
    }

    @Test
    @DisplayName("Import a profile with categories")
    void runWithCategories(RuntimeContext context) {
        context
            .withStorage()
            .withStorage("my-sample-token", "/profile-test/categories-only.json");

        var process = context.execute("ImportUserProfile", Map.of(
                "storageToken", "my-sample-token"
        ));

        process.verifyCompleted();

        context
            .verifyCategoryCreated("Salary")
            .verifyCategoryCreated("Groceries");
    }

    @Test
    @DisplayName("Import a profile with tags")
    void runWithTags(RuntimeContext context) {
        context
            .withStorage()
            .withStorage("my-sample-token", "/profile-test/tags-only.json")
            .withTags();

        var process = context.execute("ImportUserProfile", Map.of(
                "storageToken", "my-sample-token"
        ));

        process.verifyCompleted();

        context
            .verifyTagCreated("cat")
            .verifyTagCreated("dog");
    }

    @Test
    @DisplayName("Import a profile with contracts")
    void runWithContracts(RuntimeContext context) {
        var account = Mockito.spy(Account.builder()
                .id(1L)
                .name("Comcast")
                .currency("USD")
                .build());
        context
            .withAccount(account)
            .withStorage()
            .withStorage("my-sample-token", "/profile-test/contracts-only.json");

        var process = context.execute("ImportUserProfile", Map.of(
                "storageToken", "my-sample-token"
        ));

        process.verifyCompleted();
        Mockito.verify(account).createContract(
                "Cable subscription",
                "Monthly cable subscription",
                LocalDate.of(2018, 1, 1),
                LocalDate.of(2018, 12, 31));
    }

    @Test
    void runWithTransactions(RuntimeContext context) {
        context
            .withTransactions()
            .withStorage()
            .withStorage("my-sample-token", "/profile-test/transactions-only.json")
            .withAccount(Account.builder()
                        .id(1L)
                        .name("Groceries are us")
                        .currency("EUR")
                        .type("creditor")
                        .build())
            .withAccount(Account.builder()
                    .id(2L)
                    .name("My personal account")
                    .currency("EUR")
                    .type("checking")
                    .build());

        var process = context.execute("ImportUserProfile", Map.of(
                "storageToken", "my-sample-token"
        ));

        process.verifyCompleted();

        context
            .verifyTransactions(transactions ->
                    transactions.hasSize(2)
                            .anySatisfy(transaction -> {
                                Assertions.assertThat(transaction.getDescription()).isEqualTo("Some groceries");
                                Assertions.assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2018, 1, 1));
                            })
                            .anySatisfy(transaction -> {
                                Assertions.assertThat(transaction.getDescription()).isEqualTo("Some more shopping");
                                Assertions.assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2018, 2, 12));
                            }));
    }

//    @Test
//    @DisplayName("Import a profile with budgets")
//    void runWithBudgets(RuntimeContext context) {
//        context
//            .withStorage()
//            .withStorage("my-sample-token", "/profile-test/budget-only.json");
//
//        context.execute("ImportUserProfile", Map.of(
//                "storageToken", "my-sample-token"))
//                .verifyCompleted();
//    }
}
