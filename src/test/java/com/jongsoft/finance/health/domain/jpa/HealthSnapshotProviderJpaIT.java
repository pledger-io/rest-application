package com.jongsoft.finance.health.domain.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.health.domain.commands.HealthSnapshotCompleted;
import com.jongsoft.finance.health.domain.commands.HealthSnapshotCreated;
import com.jongsoft.finance.health.domain.model.HealthSnapshot;
import com.jongsoft.finance.health.types.HealthRating;
import com.jongsoft.finance.health.types.HealthSnapshotStatus;
import com.jongsoft.lang.Dates;

import jakarta.inject.Inject;

import org.assertj.core.data.TemporalUnitWithinOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;

@DisplayName("Database - Financial health")
class HealthSnapshotProviderJpaIT extends JpaTestSetup {

    @Inject
    private HealthSnapshotProviderJpa provider;

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/health-snapshot-provider.sql");
    }

    @Nested
    @DisplayName("Lookup operations for health scores")
    class LookupOperations {
        @Test
        @DisplayName("Lookup for a predefined period")
        void lookupSelectsRequestedPeriod() {
            HealthSnapshot snapshot = provider.lookup(YearMonth.of(2010, 2))
                    .getOrThrow(() -> new RuntimeException("A health snapshot should exist."));

            assertThat(snapshot.getPeriod()).isEqualTo(YearMonth.of(2010, 2));
            assertThat(snapshot.getCurrency()).isEqualTo(Currency.getInstance("USD"));
            assertThat(snapshot.getConfiguration().windowMonths()).isEqualTo(12);
            assertThat(provider.lookup(YearMonth.of(2010, 3)).isPresent()).isFalse();
        }

        @Test
        @DisplayName("Lookup for a range includes its start and excludes its end")
        void lookupSelectsSnapshotsWithinRange() {
            List<HealthSnapshot> snapshots =
                    provider.lookup(Dates.range(YearMonth.of(2010, 1), YearMonth.of(2010, 2)));

            assertThat(snapshots)
                    .extracting(HealthSnapshot::getPeriod)
                    .containsExactly(YearMonth.of(2010, 1));
        }

        @Test
        @DisplayName("Lookup for a range crosses years and skips missing months")
        void lookupRangeCrossesYearBoundaryAndSkipsMissingMonths() {
            List<HealthSnapshot> snapshots =
                    provider.lookup(Dates.range(YearMonth.of(2010, 12), YearMonth.of(2011, 5)));

            assertThat(snapshots)
                    .extracting(HealthSnapshot::getPeriod)
                    .containsExactlyInAnyOrder(
                            YearMonth.of(2010, 12), YearMonth.of(2011, 2), YearMonth.of(2011, 4));
        }

        @Test
        @DisplayName("Lookup for a range without snapshots returns an empty list")
        void lookupRangeWithoutSnapshotsReturnsEmptyList() {
            assertThat(provider.lookup(Dates.range(YearMonth.of(2011, 3), YearMonth.of(2011, 4))))
                    .isEmpty();
            assertThat(provider.lookup(Dates.range(YearMonth.of(2010, 2), YearMonth.of(2010, 2))))
                    .isEmpty();
        }

        @Test
        @DisplayName("Range lookup returns snapshots only for the authenticated user")
        void lookupRangeOnlyReturnsSnapshotsForAuthenticatedUser() {
            var range = Dates.range(YearMonth.of(2010, 1), YearMonth.of(2010, 2));

            doReturn("demo-user-not").when(authenticationFacade).authenticated();
            List<HealthSnapshot> otherUserSnapshots = provider.lookup(range);
            assertThat(otherUserSnapshots)
                    .extracting(HealthSnapshot::getPeriod)
                    .containsExactly(YearMonth.of(2010, 1));
            assertThat(otherUserSnapshots.getFirst().getCurrency())
                    .isEqualTo(Currency.getInstance("USD"));

            doReturn("demo-user").when(authenticationFacade).authenticated();
            List<HealthSnapshot> snapshots = provider.lookup(range);
            assertThat(snapshots)
                    .extracting(HealthSnapshot::getPeriod)
                    .containsExactly(YearMonth.of(2010, 1));
            assertThat(snapshots.getFirst().getCurrency()).isEqualTo(Currency.getInstance("EUR"));
        }

        @Test
        @DisplayName("Should be returned only for the correct user")
        void lookupOnlyReturnsSnapshotsForAuthenticatedUser() {
            YearMonth period = YearMonth.of(2010, 1);
            doReturn("demo-user-not").when(authenticationFacade).authenticated();
            assertThat(provider.lookup(YearMonth.of(2010, 2)).isPresent()).isFalse();

            HealthSnapshot otherUserSnapshot = provider.lookup(period)
                    .getOrThrow(() -> new RuntimeException("A health snapshot should exist."));
            assertThat(otherUserSnapshot.getCurrency()).isEqualTo(Currency.getInstance("USD"));

            doReturn("demo-user").when(authenticationFacade).authenticated();
            HealthSnapshot snapshot = provider.lookup(period)
                    .getOrThrow(() -> new RuntimeException("A health snapshot should exist."));
            assertThat(snapshot.getCurrency()).isEqualTo(Currency.getInstance("EUR"));
        }
    }

    @Nested
    @DisplayName("Health command operations")
    class HealthCommands {
        @Test
        @DisplayName("Create a new health snapshot")
        void createNewSnapshot() {
            HealthSnapshotCreated.healthSnapshotCreated(
                    YearMonth.of(2011, 1), 6, 1, Currency.getInstance("EUR"));

            HealthSnapshot snapshot = provider.lookup(YearMonth.of(2011, 1))
                    .getOrThrow(() -> new RuntimeException("A health snapshot should exist."));

            assertThat(snapshot.getPeriod()).isEqualTo(YearMonth.of(2011, 1));
            assertThat(snapshot.getBudgetAdherence().available()).isFalse();
            assertThat(snapshot.getSavingsRate().available()).isFalse();
            assertThat(snapshot.getDebtToIncome().available()).isFalse();
            assertThat(snapshot.getStatus()).isEqualTo(HealthSnapshotStatus.PROVISIONAL);
            assertThat(snapshot.getCurrency()).isEqualTo(Currency.getInstance("EUR"));
            assertThat(snapshot.getConfiguration()).satisfies(c -> {
                assertThat(c.algorithmVersion()).isEqualTo(1);
                assertThat(c.windowMonths()).isEqualTo(6);
            });
        }

        @Test
        @DisplayName("Mark existing snapshot as insufficient data")
        void snapshotInsufficientData() {
            HealthSnapshotCompleted.insufficientTransactions(YearMonth.of(2010, 2));

            HealthSnapshot snapshot = provider.lookup(YearMonth.of(2010, 2))
                    .getOrThrow(() -> new RuntimeException("A health snapshot should exist."));

            assertThat(snapshot.getStatus()).isEqualTo(HealthSnapshotStatus.INSUFFICIENT_DATA);
            assertThat(snapshot.getComputedAt())
                    .isCloseTo(
                            LocalDateTime.now(),
                            new TemporalUnitWithinOffset(2, ChronoUnit.SECONDS));

            assertThat(snapshot.getBudgetAdherence().available()).isFalse();
            assertThat(snapshot.getSavingsRate().available()).isFalse();
            assertThat(snapshot.getDebtToIncome().available()).isFalse();
        }

        @Test
        @DisplayName("Mark existing snapshot as complete")
        void snapshotCompleted() {
            HealthSnapshotCompleted.completedComputation(
                    YearMonth.of(2010, 2), HealthRating.FAIR, 1);

            HealthSnapshot snapshot = provider.lookup(YearMonth.of(2010, 2))
                    .getOrThrow(() -> new RuntimeException("A health snapshot should exist."));

            assertThat(snapshot.getStatus()).isEqualTo(HealthSnapshotStatus.COMPLETE);
            assertThat(snapshot.getHealthScore().rating()).isEqualTo(HealthRating.FAIR);
            assertThat(snapshot.getHealthScore().score()).isEqualTo(1);
            assertThat(snapshot.getComputedAt())
                    .isCloseTo(
                            LocalDateTime.now(),
                            new TemporalUnitWithinOffset(2, ChronoUnit.SECONDS));
        }
    }
}
