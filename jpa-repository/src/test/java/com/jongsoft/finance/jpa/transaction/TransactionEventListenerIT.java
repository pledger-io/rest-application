package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.transaction.*;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

class TransactionEventListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/transaction-provider.sql"
        );
        new EventBus(eventPublisher);
    }

    @Test
    void handleCreatedEvent() {
        eventPublisher.publishEvent(new CreateTransactionCommand(
                Transaction.builder()
                        .date(LocalDate.of(2020, 1, 1))
                        .currency("EUR")
                        .description("My demo transaction")
                        .transactions(Collections.List(
                                Transaction.Part.builder()
                                        .amount(25.5)
                                        .account(
                                                Account.builder()
                                                        .id(1L)
                                                        .type("default")
                                                        .build())
                                        .build(),
                                Transaction.Part.builder()
                                        .amount(-25.5)
                                        .account(
                                                Account.builder()
                                                        .id(2L)
                                                        .type("default")
                                                        .build())
                                        .build()
                        ))
                        .build()));
    }

    @Test
    void handleFailureRegistrationEvent() {
        eventPublisher.publishEvent(new RegisterFailureCommand(1L, FailureCode.POSSIBLE_DUPLICATE));

        var check = entityManager.find(TransactionJournal.class, 1L);
        Assertions.assertThat(check.getFailureCode()).isEqualTo(FailureCode.POSSIBLE_DUPLICATE);
    }

    @Test
    void handleAmountChangedEvent() {
        eventPublisher.publishEvent(new ChangeTransactionAmountCommand(
                1L,
                BigDecimal.valueOf(40.55),
                "USD"));

        var check = entityManager.find(TransactionJournal.class, 1L);
        Assertions.assertThat(check.getCurrency().getCode()).isEqualTo("USD");

        var part1 = entityManager.find(TransactionJpa.class, 1L);
        Assertions.assertThat(part1.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(40.55));

        var part2 = entityManager.find(TransactionJpa.class, 2L);
        Assertions.assertThat(part2.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(-40.55));
    }

    @Test
    void handleDescribeEvent() {
        eventPublisher.publishEvent(
                new DescribeTransactionCommand( 1L, "Updated description"));

        var check = entityManager.find(TransactionJournal.class, 1L);
        Assertions.assertThat(check.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void handleBookedEvent() {
        eventPublisher.publishEvent(new ChangeTransactionDatesCommand(
                1L,
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 3),
                LocalDate.of(2030, 1, 2)));

        var check = entityManager.find(TransactionJournal.class, 1L);
        Assertions.assertThat(check.getDate()).isEqualTo(LocalDate.of(2030, 1, 1));
        Assertions.assertThat(check.getInterestDate()).isEqualTo(LocalDate.of(2030, 1, 2));
        Assertions.assertThat(check.getBookDate()).isEqualTo(LocalDate.of(2030, 1, 3));
    }

    @Test
    void handleRelationEvent_category() {
        eventPublisher.publishEvent(new LinkTransactionCommand(
                1L,
                LinkTransactionCommand.LinkType.CATEGORY,
                "Test"));

        var check = entityManager.find(TransactionJournal.class, 1L);
        Assertions.assertThat(check.getCategory().getLabel()).isEqualTo("Test");
    }

    @Test
    void handleRelationEvent_unset() {
        eventPublisher.publishEvent(new LinkTransactionCommand(
                3L,
                LinkTransactionCommand.LinkType.CATEGORY,
                null));
        var check = entityManager.find(TransactionJournal.class, 3L);
        Assertions.assertThat(check.getCategory()).isNull();
    }

    @Test
    void handleTagEvent() {
        eventPublisher.publishEvent(new TagTransactionCommand(
                1L,
                Collections.List("Food")));

        var check = entityManager.find(TransactionJournal.class, 1L);
        Assertions.assertThat(check.getTags()).hasSize(1);
        Assertions.assertThat(check.getTags().iterator().next().getName()).isEqualTo("Food");
    }

    @Test
    void handleSplitEvent() {
        eventPublisher.publishEvent(new SplitTransactionCommand(
                1L,
                Collections.List(
                        Transaction.Part.builder()
                                .id(1L)
                                .amount(20.2)
                                .account(Account.builder().id(1L).build())
                                .build(),
                        Transaction.Part.builder()
                                .description("Changed part 1")
                                .amount(-10.1)
                                .account(Account.builder().id(2L).build())
                                .build(),
                        Transaction.Part.builder()
                                .description("Changed part 2")
                                .amount(-10.1)
                                .account(Account.builder().id(2L).build())
                                .build()
                )));

        var check = entityManager.find(TransactionJournal.class, 1L);

        var parts = Collections.List(check.getTransactions());
        Assertions.assertThat(parts.filter(part -> Objects.isNull(part.getDeleted()))).hasSize(3);
    }

    @Test
    void handleDeleteEvent() {
        eventPublisher.publishEvent(new DeleteTransactionCommand(1L));

        var check = entityManager.find(TransactionJournal.class, 1L);
        Assertions.assertThat(check.getDeleted()).isNotNull();

        var part1 = entityManager.find(TransactionJpa.class, 2L);
        var part2 = entityManager.find(TransactionJpa.class, 2L);

        Assertions.assertThat(part1.getDeleted()).isNotNull();
        Assertions.assertThat(part2.getDeleted()).isNotNull();
    }

    @MockBean
    @Replaces
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
