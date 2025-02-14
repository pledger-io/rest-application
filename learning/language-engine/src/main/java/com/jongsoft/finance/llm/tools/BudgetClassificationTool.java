package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.providers.BudgetProvider;
import dev.langchain4j.agent.tool.Tool;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;

@Singleton
@Requires(env = "ai")
public class BudgetClassificationTool implements AiTool {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(BudgetClassificationTool.class);

    private final BudgetProvider budgetProvider;

    BudgetClassificationTool(BudgetProvider budgetProvider) {
        this.budgetProvider = budgetProvider;
    }

    @Tool("Returns a list of known budgets")
    public List<String> listKnownBudgets() {
        logger.debug("Ai tool fetching available budgets.");
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        return budgetProvider.lookup(year, month)
                .stream()
                .flatMap(b -> b.getExpenses().stream())
                .map(Budget.Expense::getName)
                .toList();
    }
}
