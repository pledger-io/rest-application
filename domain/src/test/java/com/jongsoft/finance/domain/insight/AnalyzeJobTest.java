package com.jongsoft.finance.domain.insight;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.insight.CompleteAnalyzeJob;
import com.jongsoft.finance.messaging.commands.insight.CreateAnalyzeJob;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class AnalyzeJobTest {

  private ApplicationEventPublisher applicationEventPublisher;

  @BeforeEach
  void setup() {
    applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
    new EventBus(applicationEventPublisher);
  }

  @Test
  void shouldCompleteAnalyzeJobSuccessfully() {
    // Arrange
    YearMonth month = YearMonth.of(2023, 9);
    AnalyzeJob analyzeJob = new AnalyzeJob(month);
    var mockedCompleteAnalyzeJob = mockStatic(CompleteAnalyzeJob.class);

    // Act
    analyzeJob.complete();

    // Assert
    assertTrue(analyzeJob.isCompleted(), "Expected analyze job to be marked as completed.");
    mockedCompleteAnalyzeJob.verify(() -> CompleteAnalyzeJob.completeAnalyzeJob(month));
    mockedCompleteAnalyzeJob.close();
  }

  @Test
  void shouldThrowExceptionWhenCompletingAlreadyCompletedJob() {
    // Arrange
    YearMonth month = YearMonth.of(2023, 9);
    AnalyzeJob analyzeJob = new AnalyzeJob(month);
    analyzeJob.complete();

    // Act & Assert
    StatusException exception = assertThrows(
        StatusException.class,
        analyzeJob::complete,
        "Expected exception when completing an already completed analyze job."
    );
    assertEquals(
        "Cannot complete an analyze job that has already completed.",
        exception.getMessage(),
        "Exception message mismatch"
    );
  }

  @Test
  void shouldCreateAnalyzeJobOnConstruction() {
    // Arrange
    YearMonth month = YearMonth.of(2023, 9);
    var mockedCreateAnalyzeJob = mockStatic(CreateAnalyzeJob.class);

    // Act
    new AnalyzeJob(month);

    // Assert
    mockedCreateAnalyzeJob.verify(() -> CreateAnalyzeJob.createAnalyzeJob(month));
    mockedCreateAnalyzeJob.close();
  }
}
