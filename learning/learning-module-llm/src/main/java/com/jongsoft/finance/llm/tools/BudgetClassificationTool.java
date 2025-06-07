package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.providers.BudgetProvider;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;

@Singleton
@AiEnabled
public class BudgetClassificationTool implements AiTool {

  private final Logger logger = org.slf4j.LoggerFactory.getLogger(BudgetClassificationTool.class);

  private final BudgetProvider budgetProvider;

  BudgetClassificationTool(BudgetProvider budgetProvider) {
    this.budgetProvider = budgetProvider;
  }

  @Tool(
      """
            This tool returns the full list of known categories that can be used when classifying financial transactions.

            Use this tool to retrieve or confirm the set of valid categories.
            Do not use any category that is not included in the output of this tool.

            To view subcategories or tags, use the appropriate tools designed for those purposes.""")
  public List<String> listKnownCategories() {
    logger.trace("Ai tool fetching available budgets.");
    int year = LocalDate.now().getYear();
    int month = LocalDate.now().getMonthValue();

    return budgetProvider
        .lookup(year, month)
        .map(b -> b.getExpenses().stream().map(Budget.Expense::getName))
        .getOrSupply(Stream::of)
        .toList();
  }
}
