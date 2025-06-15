package com.jongsoft.finance.jpa.insight;

import com.jongsoft.finance.domain.insight.InsightType;
import com.jongsoft.finance.domain.insight.Severity;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.messaging.commands.insight.CreateSpendingInsight;
import com.jongsoft.finance.providers.SpendingInsightProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import io.micronaut.test.annotation.MockBean;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

class SpendingInsightProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private SpendingInsightProvider spendingInsightProvider;

    @Inject
    private FilterFactory filterFactory;

    @Inject
    private ApplicationEventPublisher<CreateSpendingInsight> eventPublisher;

    @BeforeEach
    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/insight/spending-insight-provider.sql"
        );
    }

    @Test
    @DisplayName("Test lookup() - Should return all insights for authenticated user")
    void lookup() {
        var insights = spendingInsightProvider.lookup();

        Assertions.assertThat(insights).hasSize(4);
        Assertions.assertThat(insights.stream().map(SpendingInsight::getCategory).toList())
                .contains("Groceries", "Entertainment", "Dining");
    }

    @Test
    @DisplayName("Test lookup(String category) - Should return insight for specific category")
    void lookupByCategory() {
        var insight = spendingInsightProvider.lookup("Entertainment");

        Assertions.assertThat(insight.isPresent()).isTrue();
        Assertions.assertThat(insight.get().getType()).isEqualTo(InsightType.BUDGET_EXCEEDED);
        Assertions.assertThat(insight.get().getSeverity()).isEqualTo(Severity.ALERT);
        Assertions.assertThat(insight.get().getScore()).isEqualTo(0.95);
    }

    @Test
    @DisplayName("Test lookup(YearMonth yearMonth) - Should return insights for specific month")
    void lookupByYearMonth() {
        var insights = spendingInsightProvider.lookup(YearMonth.of(2023, 1));

        Assertions.assertThat(insights).hasSize(2);
        Assertions.assertThat(insights.stream().map(SpendingInsight::getCategory).toList())
                .contains("Groceries", "Entertainment");
    }

    @Test
    @DisplayName("Test lookup(FilterCommand) - Should return filtered insights")
    void lookupWithFilter() {
        var filter = filterFactory.insight()
                .category("Groceries", true);

        var result = spendingInsightProvider.lookup(filter);

        Assertions.assertThat(result.content()).hasSize(2);
        Assertions.assertThat(result.content().stream().map(SpendingInsight::getType).toList())
                .contains(InsightType.UNUSUAL_AMOUNT, InsightType.UNUSUAL_FREQUENCY);
    }

    @Test
    @DisplayName("Test save() - Should save a new insight")
    void save() {
        // Create metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("test_key", "test_value");
        metadata.put("numeric_value", 123);

        // Create a new insight
        SpendingInsight newInsight = SpendingInsight.builder()
                .type(InsightType.UNUSUAL_MERCHANT)
                .category("Shopping")
                .severity(Severity.INFO)
                .score(0.7)
                .detectedDate(LocalDate.of(2023, 3, 15))
                .message("Unusual merchant detected for Shopping")
                .metadata(metadata)
                .build();

        // Save the insight
        eventPublisher.publishEvent(new CreateSpendingInsight(newInsight));

        // Verify it was saved by looking it up
        var savedInsight = spendingInsightProvider.lookup("Shopping");

        Assertions.assertThat(savedInsight.isPresent()).isTrue();
        Assertions.assertThat(savedInsight.get().getType()).isEqualTo(InsightType.UNUSUAL_MERCHANT);
        Assertions.assertThat(savedInsight.get().getMessage()).isEqualTo("Unusual merchant detected for Shopping");
        Assertions.assertThat(savedInsight.get().getMetadata()).containsEntry("test_key", "test_value");
        Assertions.assertThat(savedInsight.get().getMetadata()).containsEntry("numeric_value", "123");
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
