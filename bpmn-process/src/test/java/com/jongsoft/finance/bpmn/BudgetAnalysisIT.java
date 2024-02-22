package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.bpmn.process.ProcessExtension;
import com.jongsoft.finance.bpmn.process.RuntimeContext;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.lang.Collections;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@ProcessExtension
@DisplayName("Budget analysis feature")
public class BudgetAnalysisIT {

    @Inject
    private SettingProvider applicationSettings;

    @Test
    @DisplayName("Budget analysis without a recorded deviation")
    void budgetWithoutDeviation(RuntimeContext context) {
        context
                .withBudget(2019, 1, Budget.builder()
                        .expenses(Collections.List(
                                Budget.Expense.builder()
                                        .id(1L)
                                        .lowerBound(75)
                                        .upperBound(100)
                                        .name("Groceries")
                                        .build()))
                        .id(1L)
                        .build())
                .withTransactionPages()
                .thenReturn(ResultPage.of(
                        buildTransaction(50.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(20.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(2, "Groceries", "My Account", "To Account")
                ))
                .thenReturn(ResultPage.of(
                        buildTransaction(12.20, "Groceries", "My Account", "To Account"),
                        buildTransaction(40.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(12, "Groceries", "My Account", "To Account")
                ))
                .thenReturn(ResultPage.of(
                        buildTransaction(50, "Groceries", "My Account", "To Account"),
                        buildTransaction(12, "Groceries", "My Account", "To Account"),
                        buildTransaction(10, "Groceries", "My Account", "To Account")
                ));

        Mockito.when(applicationSettings.getMaximumBudgetDeviation()).thenReturn(0.50);
        Mockito.when(applicationSettings.getBudgetAnalysisMonths()).thenReturn(3);

        context.execute("budget_analysis", Variables.createVariables()
                        .putValue("id", 1L)
                        .putValue("scheduled", "2019-01-01"))
                .verifyCompleted()
                .<Boolean>yankVariables("deviates", value ->
                        value.hasSize(2)
                                .allMatch(a-> !a));

    }

    @Test
    @DisplayName("Budget analysis with a recorded deviation")
    void budgetWithDeviation(RuntimeContext context) {
        context
                .withBudget(2019, 1, Budget.builder()
                        .expenses(Collections.List(
                                Budget.Expense.builder()
                                        .id(1L)
                                        .lowerBound(75)
                                        .upperBound(100)
                                        .name("Groceries")
                                        .build()))
                        .id(1L)
                        .build())
                .withTransactionPages()
                .thenReturn(ResultPage.of(
                        buildTransaction(50.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(20.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(30.2, "Groceries", "My Account", "To Account")
                ))
                .thenReturn(ResultPage.of(
                        buildTransaction(12.20, "Groceries", "My Account", "To Account"),
                        buildTransaction(40.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(123.2, "Groceries", "My Account", "To Account")
                ))
                .thenReturn(ResultPage.of(
                        buildTransaction(4, "Groceries", "My Account", "To Account"),
                        buildTransaction(12, "Groceries", "My Account", "To Account"),
                        buildTransaction(13, "Groceries", "My Account", "To Account")
                ));

        Mockito.when(applicationSettings.getBudgetAnalysisMonths()).thenReturn(3);
        Mockito.when(applicationSettings.getMaximumBudgetDeviation()).thenReturn(0.05);

        var execution = context.execute("budget_analysis", Variables.createVariables()
                        .putValue("id", 1L)
                        .putValue("scheduled", "2019-01-01"))
                .<Boolean>yankVariables("deviates", value -> value.hasSize(2).allMatch(a-> a))
                .<Number>yankVariables("deviation", value -> value.allMatch(v -> v.equals(-14.23)));

        execution.task("handle_deviation")
                .verifyVariable("needed_correction", a -> Assertions.assertThat(a).isEqualTo(-14.23));
    }

    public static Transaction buildTransaction(double amount, String description, String to, String from) {
        return Transaction.builder()
                .description(description)
                .transactions(Collections.List(
                        Transaction.Part.builder()
                                .amount(amount)
                                .account(Account.builder()
                                        .id(1L)
                                        .name(to)
                                        .build())
                                .build(),
                        Transaction.Part.builder()
                                .amount(-amount)
                                .account(Account.builder()
                                        .id(2L)
                                        .name(from)
                                        .build())
                                .build()
                ))
                .build();
    }

}
