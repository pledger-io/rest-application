package com.jongsoft.finance.bpmn.feature;

import com.jongsoft.finance.bpmn.feature.junit.ProcessExtension;
import com.jongsoft.finance.bpmn.feature.junit.RuntimeContext;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.lang.Control;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.commons.lang3.mutable.MutableObject;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;

@MicronautTest
@ProcessExtension
@DisplayName("Account reconciliation feature")
class AccountReconcileIT {


    @Test
    @DisplayName("Account reconcile with mismatching starting balance.")
    void run_differentStartBalance(RuntimeContext context) {
        Account reconcileAccount = Mockito.spy(Account.builder().id(1L).type("checking").name("Example Account").build());

        context
                .withAccount(reconcileAccount)
                .withReconcileAccount();

        context.withBalance()
                .thenReturn(Control.Option(BigDecimal.ZERO))
                .thenReturn(Control.Option(BigDecimal.valueOf(10)))
                .thenReturn(Control.Option(BigDecimal.valueOf(-20.0)));

        var process = context.execute("AccountReconcile",
                Variables.createVariables()
                        .putValue("startDate", "2019-01-01")
                        .putValue("endDate", "2019-12-31")
                        .putValue("openBalance", "10.0")
                        .putValue("endBalance", "100.2")
                        .putValue("accountId", 1L));

        // Then: the process should be suspended
        process.task("task_reconcile_before")
                .verifyVariable("computedStartBalance",
                        value -> Assertions.assertThat(value).isEqualTo(BigDecimal.ZERO))
                .complete();

        process.verifyCompleted();
        context.verifyTransactions(transactions ->
                transactions.hasSize(1)
                        .as("Transaction should be 120.")
                        .anySatisfy(transaction -> {
                            Assertions.assertThat(transaction.getTransactions())
                                    .hasSize(2)
                                    .anySatisfy(part -> {
                                        Assertions.assertThat(part.getAmount())
                                                .isEqualTo(-120.2D);
                                        Assertions.assertThat(part.getAccount().getType())
                                                .isEqualTo("reconcile");
                                    })
                                    .anySatisfy(part -> {
                                        Assertions.assertThat(part.getAmount())
                                                .isEqualTo(120.2D);
                                        Assertions.assertThat(part.getAccount())
                                                .isEqualTo(reconcileAccount);
                                    });
                        }));
    }

    @Test
    @DisplayName("Account reconcile with different end balance creates a balancing transaction.")
    void run_endBalanceOff(RuntimeContext context) {
        Account reconcileAccount = Mockito.spy(Account.builder().id(1L).type("checking").name("Example Account").build());

        context
                .withAccount(reconcileAccount)
                .withReconcileAccount()
                .withBalance()
                .thenReturn(Control.Option(BigDecimal.ZERO))
                .thenReturn(Control.Option(BigDecimal.valueOf(-20.0)));

        var computedStart = new MutableObject<BigDecimal>();
        var computedEnd = new MutableObject<BigDecimal>();
        var difference = new MutableObject<BigDecimal>();
        context.execute(
                "AccountReconcile",
                Variables.createVariables()
                        .putValue("startDate", "2019-01-01")
                        .putValue("endDate", "2019-12-31")
                        .putValue("openBalance", "0")
                        .putValue("endBalance", "100.2")
                        .putValue("accountId", 1L))
                .verifyCompleted()
                .yankVariable("computedStartBalance", computedStart::setValue)
                .yankVariable("computedEndBalance", computedEnd::setValue)
                .yankVariable("balanceDifference", difference::setValue);

        Assertions.assertThat(computedStart.getValue()).isEqualTo(BigDecimal.ZERO);
        Assertions.assertThat(computedEnd.getValue()).isEqualTo(BigDecimal.valueOf(-20.0));
        Assertions.assertThat(difference.getValue().setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(BigDecimal.valueOf(-120.2));

        context.verifyTransactions(transactions ->
                transactions.hasSize(1)
                        .anySatisfy(transaction -> {
                            Assertions.assertThat(transaction.getTransactions())
                                    .hasSize(2)
                                    .as("The transaction should have two parts.")
                                    .anySatisfy(part -> {
                                        Assertions.assertThat(part.getAmount())
                                                .isEqualTo(-120.2D);
                                        Assertions.assertThat(part.getAccount().getType())
                                                .isEqualTo("reconcile");
                                    })
                                    .anySatisfy(part -> {
                                        Assertions.assertThat(part.getAmount())
                                                .isEqualTo(120.2D);
                                        Assertions.assertThat(part.getAccount())
                                                .isEqualTo(reconcileAccount);
                                    });
                        }));
    }

    @Test
    @DisplayName("Account reconcile with no differences.")
    void runWithNoDifferences(RuntimeContext context) {
        Account reconcileAccount = Mockito.spy(Account.builder().id(1L).type("checking").name("Example Account").build());

        context
                .withAccount(reconcileAccount)
                .withReconcileAccount()
                .withBalance()
                .thenReturn(Control.Option(BigDecimal.ZERO))
                .thenReturn(Control.Option(BigDecimal.valueOf(-20)));


        context.execute(
                "AccountReconcile",
                Variables.createVariables()
                        .putValue("startDate", "2019-01-01")
                        .putValue("endDate", "2019-12-31")
                        .putValue("openBalance", "0")
                        .putValue("endBalance", "-20")
                        .putValue("accountId", 1L))
                .verifyCompleted();

        context.verifyTransactions(transactions ->
                transactions.hasSize(0));
    }
}
