package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.llm.AITool;
import com.jongsoft.finance.providers.BudgetProvider;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class BudgetTool implements AITool {
    private static final Logger logger = getLogger(BudgetTool.class);
    private final Pattern datePattern = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})");

    private final BudgetProvider budgetProvider;

    public BudgetTool(BudgetProvider budgetProvider) {
        this.budgetProvider = budgetProvider;
    }

    @Tool("Returns a the known classifications")
    public List<String> getAvailableBudgets(@P("The date of the transaction in the format yyyy-MM. Leave blank if no date is known.") String date) {
        logger.info("Fetching available budgets valid on {}.", date);

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        if (date != null && !date.isBlank()) {
            var matcher = datePattern.matcher(date);
            if (matcher.matches()) {
                year = Integer.parseInt(matcher.group("year"));
                month = Integer.parseInt(matcher.group("month"));
            }
        }

        return budgetProvider.lookup(year, month)
                .stream()
                .flatMap(b -> b.getExpenses().stream())
                .map(Budget.Expense::getName)
                .toList();
    }
}
