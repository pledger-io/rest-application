package com.jongsoft.finance.jpa.insight;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.insight.CompleteAnalyzeJob;
import com.jongsoft.finance.messaging.commands.insight.CreateAnalyzeJob;
import com.jongsoft.finance.providers.AnalyzeJobProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import io.micronaut.test.annotation.MockBean;

import java.time.YearMonth;

class AnalyzeJobProviderJpaIT extends JpaTestSetup {

  @Inject
  private AuthenticationFacade authenticationFacade;

  @Inject
  private AnalyzeJobProvider analyzeJobProvider;

  @Inject
  private ApplicationEventPublisher eventPublisher;

  @Inject
  private EntityManager entityManager;

  @BeforeEach
  void setup() {
    Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
    loadDataset(
        "sql/clean-up.sql",
        "sql/base-setup.sql"
    );

    new EventBus(eventPublisher);
  }

  @Test
  @DisplayName("Test first() - Should return the first uncompleted analyze job")
  void first() {
    loadDataset(
        "sql/insight/analyze-job-provider.sql"
    );
    var job = analyzeJobProvider.first();

    Assertions.assertThat(job).isPresent();
    Assertions.assertThat(job.get().getJobId()).isEqualTo("job-1");
    Assertions.assertThat(job.get().getMonth()).isEqualTo(YearMonth.of(2023, 1));
    Assertions.assertThat(job.get().isCompleted()).isFalse();
  }

  @Test
  @DisplayName("Test first() - Should return empty when no uncompleted jobs exist")
  void firstNoJobs() {
    // Load a different dataset with only completed jobs
    loadDataset(
        "sql/insight/analyze-job-provider-completed.sql"
    );

    var job = analyzeJobProvider.first();

    Assertions.assertThat(job).isEmpty();
  }

  @Test
  @DisplayName("Test createJob() - Should create a new analyze job")
  void createJob() {
    CreateAnalyzeJob.createAnalyzeJob(YearMonth.now());

    var entity = entityManager.createQuery("select j from AnalyzeJobJpa j where j.yearMonth = :month", AnalyzeJobJpa.class)
        .setParameter("month", YearMonth.now().toString())
        .getSingleResult();

    Assertions.assertThat(entity).isNotNull();
    Assertions.assertThat(entity.getYearMonth()).isEqualTo(YearMonth.now().toString());
    Assertions.assertThat(entity.isCompleted()).isFalse();
  }

  @Test
  @DisplayName("Test completeJob() - Should set completed flag")
  void completeJob() {
    loadDataset(
        "sql/insight/analyze-job-provider.sql"
    );

    CompleteAnalyzeJob.completeAnalyzeJob(YearMonth.of(2023, 1));

    var entity = entityManager.createQuery("select j from AnalyzeJobJpa j where j.yearMonth = :month", AnalyzeJobJpa.class)
        .setParameter("month", "2023-01")
        .getSingleResult();

    Assertions.assertThat(entity).isNotNull();
    Assertions.assertThat(entity.getYearMonth()).isEqualTo(YearMonth.of(2023, 1).toString());
    Assertions.assertThat(entity.isCompleted()).isTrue();
  }

  @MockBean
  AuthenticationFacade authenticationFacade() {
    return Mockito.mock(AuthenticationFacade.class);
  }
}
