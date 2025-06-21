package com.jongsoft.finance.domain.insight;

import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.insight.CreateSpendingPattern;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SpendingPatternTest {

  private ApplicationEventPublisher applicationEventPublisher;

  @BeforeEach
  void setup() {
    applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
    new EventBus(applicationEventPublisher);
  }

  @Test
  void sameShouldEqual() {
    SpendingPattern pattern1 = SpendingPattern.builder()
        .type(PatternType.RECURRING_WEEKLY)
        .category("Food")
        .confidence(0.9)
        .detectedDate(LocalDate.of(2023, 10, 1))
        .metadata(new HashMap<>())
        .build();

    SpendingPattern pattern2 = SpendingPattern.builder()
        .type(PatternType.RECURRING_WEEKLY)
        .category("Food")
        .confidence(0.8) // Different confidence does not affect equality
        .detectedDate(LocalDate.of(2023, 10, 1))
        .metadata(new HashMap<>())
        .build();

    assertEquals(pattern1, pattern2);
  }

  @Test
  void differentShouldNotEqual() {
    SpendingPattern pattern1 = SpendingPattern.builder()
        .type(PatternType.RECURRING_WEEKLY)
        .category("Food")
        .confidence(0.9)
        .detectedDate(LocalDate.of(2023, 10, 1))
        .metadata(new HashMap<>())
        .build();

    SpendingPattern pattern2 = SpendingPattern.builder()
        .type(PatternType.RECURRING_MONTHLY) // Different type
        .category("Travel") // Different category
        .confidence(0.9)
        .detectedDate(LocalDate.of(2023, 9, 30)) // Different detected date
        .metadata(new HashMap<>())
        .build();

    assertNotEquals(pattern1, pattern2);
  }

  @Test
  void signalShouldCreate() {
    var pattern = SpendingPattern.builder()
        .type(PatternType.RECURRING_WEEKLY)
        .category("Food")
        .confidence(0.9)
        .detectedDate(LocalDate.of(2023, 10, 1))
        .metadata(new HashMap<>())
        .build();

    // Act
    pattern.signal();

    var captor = ArgumentCaptor.forClass(CreateSpendingPattern.class);
    Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());

    var command = captor.getValue();
    assertThat(command.type()).isEqualTo(pattern.getType());
    assertThat(command.category()).isEqualTo(pattern.getCategory());
    assertThat(command.confidence()).isEqualTo(pattern.getConfidence());
    assertThat(command.detectedDate()).isEqualTo(pattern.getDetectedDate());
    assertThat(command.metadata()).isEqualTo(pattern.getMetadata());
  }
}
