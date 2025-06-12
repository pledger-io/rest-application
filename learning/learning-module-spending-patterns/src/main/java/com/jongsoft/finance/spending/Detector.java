package com.jongsoft.finance.spending;

import com.jongsoft.finance.domain.insight.Insight;
import com.jongsoft.finance.domain.transaction.Transaction;

import java.util.List;

public interface Detector<T extends Insight> {

  void updateBaseline();
  List<T> detect(Transaction transaction);

}
