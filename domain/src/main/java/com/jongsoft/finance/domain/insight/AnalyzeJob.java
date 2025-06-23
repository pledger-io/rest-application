package com.jongsoft.finance.domain.insight;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.messaging.commands.insight.CompleteAnalyzeJob;
import com.jongsoft.finance.messaging.commands.insight.CreateAnalyzeJob;
import java.time.YearMonth;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AnalyzeJob {

  private final String jobId;
  private final YearMonth month;
  private boolean completed;

  public AnalyzeJob(YearMonth month) {
    this.jobId = UUID.randomUUID().toString();
    this.month = month;

    CreateAnalyzeJob.createAnalyzeJob(month);
  }

  public void complete() {
    if (completed) {
      throw StatusException.badRequest(
          "Cannot complete an analyze job that has already completed.", "AnalyzeJob.completed");
    }

    completed = true;
    CompleteAnalyzeJob.completeAnalyzeJob(month);
  }
}
