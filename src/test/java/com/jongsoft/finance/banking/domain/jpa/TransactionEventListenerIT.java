package com.jongsoft.finance.banking.domain.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionJournal;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionJpa;
import com.jongsoft.finance.banking.domain.jpa.mapper.AccountMapper;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.banking.types.FailureCode;
import com.jongsoft.finance.banking.types.TransactionLinkType;
import com.jongsoft.lang.Collections;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@DisplayName("Database - Transaction mutations")
class TransactionEventListenerIT extends JpaTestSetup {

    @Inject
    private AccountMapper accountMapper;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/transaction-provider.sql");
    }

    @Test
    @DisplayName("Create new transaction")
    void handleCreatedEvent() {
        var fromAccount = accountMapper.toDomain(entityManager.find(AccountJpa.class, 1L));
        var toAccount = accountMapper.toDomain(entityManager.find(AccountJpa.class, 2L));

        Transaction.create(fromAccount, toAccount, LocalDate.now(), 25.5, "My demo transaction")
                .register();
    }

    @Test
    @DisplayName("Register failure")
    void handleFailureRegistrationEvent() {
        RegisterFailureCommand.registerFailure(1L, FailureCode.POSSIBLE_DUPLICATE);

        var check = entityManager.find(TransactionJournal.class, 1L);
        assertThat(check.getFailureCode()).isEqualTo(FailureCode.POSSIBLE_DUPLICATE);
    }

    @Test
    @DisplayName("Change amount")
    void handleAmountChangedEvent() {
        ChangeTransactionAmountCommand.amountChanged(1L, BigDecimal.valueOf(40.55), "USD");

        var check = entityManager.find(TransactionJournal.class, 1L);
        assertThat(check.getCurrency().getCode()).isEqualTo("USD");

        var part1 = entityManager.find(TransactionJpa.class, 1L);
        assertThat(part1.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(40.55));

        var part2 = entityManager.find(TransactionJpa.class, 2L);
        assertThat(part2.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(-40.55));
    }

    @Test
    @DisplayName("Change description")
    void handleDescribeEvent() {
        DescribeTransactionCommand.transactionDescribed(1L, "Updated description");

        var check = entityManager.find(TransactionJournal.class, 1L);
        assertThat(check.getDescription()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("Change dates")
    void handleBookedEvent() {
        ChangeTransactionDatesCommand.transactionDatesChanged(
                1L, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 3), LocalDate.of(2030, 1, 2));

        var check = entityManager.find(TransactionJournal.class, 1L);
        assertThat(check.getDate()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(check.getInterestDate()).isEqualTo(LocalDate.of(2030, 1, 2));
        assertThat(check.getBookDate()).isEqualTo(LocalDate.of(2030, 1, 3));
    }

    @Test
    @DisplayName("Set relation category")
    void handleRelationEvent_category() {
        LinkTransactionCommand.linkCreated(1L, TransactionLinkType.CATEGORY, 2L);

        var check = entityManager.find(TransactionJournal.class, 1L);
        assertThat(check.getMetadata()).anySatisfy(metadata -> assertThat(metadata)
                .hasFieldOrPropertyWithValue("entityId", 2L)
                .hasFieldOrPropertyWithValue("relationType", "CATEGORY"));
    }

    @Test
    @DisplayName("Unset relation")
    void handleRelationEvent_unset() {
        LinkTransactionCommand.linkCreated(3L, TransactionLinkType.CATEGORY, null);
        var check = entityManager.find(TransactionJournal.class, 3L);

        assertThat(check.getMetadata())
                .noneSatisfy(
                        metadata -> assertThat(metadata.getRelationType()).isEqualTo("CATEGORY"));
    }

    @Test
    @DisplayName("Add tag")
    void handleTagEvent() {
        TagTransactionCommand.tagCreated(1L, Collections.List("Food"));

        var check = entityManager.find(TransactionJournal.class, 1L);
        assertThat(check.getTags()).hasSize(1);
        assertThat(check.getTags().iterator().next().getName()).isEqualTo("Food");
    }

    @Test
    @DisplayName("Split transaction")
    void handleSplitEvent() {
        var fromAccount = accountMapper.toDomain(entityManager.find(AccountJpa.class, 1L));
        var toAccount = accountMapper.toDomain(entityManager.find(AccountJpa.class, 2L));

        SplitTransactionCommand.transactionSplit(
                1L,
                List.of(
                        Transaction.Part.create(fromAccount, 20.2, ""),
                        Transaction.Part.create(toAccount, -10.1, "Changed part 1"),
                        Transaction.Part.create(toAccount, -10.1, "Changed part 2")));

        var check = entityManager.find(TransactionJournal.class, 1L);

        var parts = Collections.List(check.getTransactions());
        assertThat(parts.filter(part -> Objects.isNull(part.getDeleted()))).hasSize(3);
    }

    @Test
    @DisplayName("Delete transaction")
    void handleDeleteEvent() {
        DeleteTransactionCommand.transactionDeleted(1L);

        var check = entityManager.find(TransactionJournal.class, 1L);
        assertThat(check.getDeleted()).isNotNull();

        var part1 = entityManager.find(TransactionJpa.class, 2L);
        var part2 = entityManager.find(TransactionJpa.class, 2L);

        assertThat(part1.getDeleted()).isNotNull();
        assertThat(part2.getDeleted()).isNotNull();
    }
}
