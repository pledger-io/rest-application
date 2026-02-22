package com.jongsoft.finance.spending.domain.service.detector;

import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.spending.domain.model.Insight;

import java.time.YearMonth;
import java.util.List;

public interface Detector<T extends Insight> {

    void updateBaseline(YearMonth forMonth);

    void analysisCompleted();

    List<T> detect(Transaction transaction);

    boolean readyForAnalysis();
}
