package com.jongsoft.finance.spending;

import com.jongsoft.finance.domain.insight.Insight;
import com.jongsoft.finance.domain.transaction.Transaction;
import java.time.YearMonth;
import java.util.List;

public interface Detector<T extends Insight> {

  void updateBaseline(YearMonth forMonth);

  void analysisCompleted();

  List<T> detect(Transaction transaction);

  boolean readyForAnalysis();
}
