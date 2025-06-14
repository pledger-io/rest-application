package com.jongsoft.finance.spending.detector.anomaly;

import com.jongsoft.finance.domain.insight.Severity;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;
import java.util.Optional;

public interface Anomaly {

  Optional<SpendingInsight> detect(Transaction transaction, UserCategoryStatistics statistics);

  default Severity getSeverityFromScore(double score) {
    if (score >= 0.8) {
      return Severity.ALERT;
    } else if (score >= 0.5) {
      return Severity.WARNING;
    } else {
      return Severity.INFO;
    }
  }
}
