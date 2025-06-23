package com.jongsoft.finance.domain.insight;

import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.insight.CreateSpendingInsight;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SpendingInsightTest {

  private ApplicationEventPublisher applicationEventPublisher;

  @BeforeEach
  void setup() {
    applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
    new EventBus(applicationEventPublisher);
  }

  @Test
  void shouldCallCreateSpendingInsightWithCorrectInstance() {
    SpendingInsight insight = SpendingInsight.builder()
        .type(InsightType.SPENDING_SPIKE)
        .category("Food")
        .severity(Severity.WARNING)
        .score(75.5)
        .transactionId(12345L)
        .detectedDate(LocalDate.now())
        .message("High spending detected on food")
        .metadata(new HashMap<>(Map.of("limitExceeded", true)))
        .build();

    // Act
    insight.signal();

    // Assert
    var captor = ArgumentCaptor.forClass(CreateSpendingInsight.class);
    Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());

    var command = captor.getValue();
    assertThat(command.type()).isEqualTo(insight.getType());
    assertThat(command.category()).isEqualTo(insight.getCategory());
    assertThat(command.severity()).isEqualTo(insight.getSeverity());
    assertThat(command.score()).isEqualTo(insight.getScore());
    assertThat(command.transactionId()).isEqualTo(insight.getTransactionId());
    assertThat(command.detectedDate()).isEqualTo(insight.getDetectedDate());
    assertThat(command.message()).isEqualTo(insight.getMessage());
    assertThat(command.metadata()).isEqualTo(insight.getMetadata());
  }
}
