package com.jongsoft.finance.rule;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.learning.SuggestionEngine;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.SuggestionResult;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
class RuleSuggestionEngine implements SuggestionEngine {

  private final RuleEngine ruleEngine;

  RuleSuggestionEngine(RuleEngine ruleEngine) {
    this.ruleEngine = ruleEngine;
  }

  @Override
  public SuggestionResult makeSuggestions(SuggestionInput transactionInput) {
    var ruleDataset = new RuleDataSet();
    if (transactionInput.description() != null) {
      ruleDataset.put(RuleColumn.DESCRIPTION, transactionInput.description());
    }
    if (transactionInput.fromAccount() != null) {
      ruleDataset.put(RuleColumn.SOURCE_ACCOUNT, transactionInput.fromAccount());
    }
    if (transactionInput.toAccount() != null) {
      ruleDataset.put(RuleColumn.TO_ACCOUNT, transactionInput.toAccount());
    }
    ruleDataset.put(RuleColumn.AMOUNT, transactionInput.amount());

    var ruleOutput = ruleEngine.run(ruleDataset);
    return new SuggestionResult(
        ruleOutput.getCasted(RuleColumn.BUDGET),
        ruleOutput.getCasted(RuleColumn.CATEGORY),
        List.of());
  }
}
