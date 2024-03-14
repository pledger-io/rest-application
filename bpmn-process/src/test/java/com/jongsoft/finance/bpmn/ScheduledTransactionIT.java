package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.bpmn.process.ProcessExtension;
import com.jongsoft.finance.bpmn.process.RuntimeContext;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@MicronautTest
@ProcessExtension
@DisplayName("Scheduled Transaction feature")
class ScheduledTransactionIT {

    @Test
    void scheduleRun(RuntimeContext context) {
        context
                .withStorage()
                .withTransactions()
                .withAccount(Account.builder()
                        .id(1L)
                        .name("Source account")
                        .type("checking")
                        .build())
                .withAccount(Account.builder()
                        .id(2L)
                        .name("Cable Company")
                        .type("creditor")
                        .build())
                .withTransactionSchedule(ScheduledTransaction.builder()
                        .id(1L)
                        .name("CableCom")
                        .amount(29.99)
                        .description("Monthly TV")
                        .source(Account.builder().id(1L).build())
                        .destination(Account.builder().id(2L).build())
                        .build());

        context.execute("ScheduledTransaction", Variables.createVariables()
                        .putValue("id", 1L)
                        .putValue("scheduled", "2019-01-01"))
                .verifyCompleted();

        context.verifyTransactions(transactions ->
                transactions.hasSize(1)
                        .anySatisfy(transaction -> {
                            Assertions.assertThat(transaction.computeTo().getId()).isEqualTo(2L);
                            Assertions.assertThat(transaction.computeFrom().getId()).isEqualTo(1L);
                            Assertions.assertThat(transaction.computeAmount(transaction.computeTo())).isEqualTo(29.99);
                            Assertions.assertThat(transaction.getDescription()).isEqualTo("CableCom: Monthly TV");
                            Assertions.assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2019, 1, 1));
                        }));
    }
}
