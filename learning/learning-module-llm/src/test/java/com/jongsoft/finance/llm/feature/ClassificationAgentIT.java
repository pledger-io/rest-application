package com.jongsoft.finance.llm.feature;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.learning.SuggestionEngine;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnabledIfEnvironmentVariable(named = "AI_ENABLED", matches = "true")
class ClassificationAgentIT extends AiBase {

    @Inject
    private SuggestionEngine suggestionEngine;

    @Inject
    private CategoryProvider categoryProvider;

    @Inject
    private BudgetProvider budgetProvider;

    @Inject
    private TagProvider tagProvider;

    private List<String> categories = new ArrayList<>();
    private List<String> budgets = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    @Test
    void verifyClassificationOfTransactions_en() throws IOException, CsvValidationException {
        prepareTestData("en");
        runTestCases("en");
    }

    @Test
    void verifyClassificationOfTransactions_nl() throws IOException, CsvValidationException {
        prepareTestData("nl");
        runTestCases("nl");
    }

    private void runTestCases(String language) throws IOException, CsvValidationException {
        try (var testCases = getClass().getResourceAsStream("/" + language + "/sample-transactions.csv");
             var csvReader = new CSVReaderBuilder(new InputStreamReader(testCases)).build()) {
            csvReader.skip(1);

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                var date = line[0];
                var description = line[1];
                var from = line[2];
                var to = line[3];
                var amount = Double.parseDouble(line[4]);

                MDC.put("correlationId", UUID.randomUUID().toString());

                var suggestionInput = new SuggestionInput(LocalDate.parse(date), description, from, to, amount);
                var suggestions = suggestionEngine.makeSuggestions(suggestionInput);

                if (!suggestions.budget().isEmpty() && !budgets.contains(suggestions.budget())) {
                    LoggerFactory.getLogger(getClass()).error("Chosen budget not allowed: {}", suggestions.budget());
                } else {
                    LoggerFactory.getLogger(getClass()).info("Chosen budget: {}", suggestions.budget());
                }

                if (!suggestions.category().isEmpty() && !categories.contains(suggestions.category())) {
                    LoggerFactory.getLogger(getClass()).error("Chosen category not allowed: {}", suggestions.category());
                } else {
                    LoggerFactory.getLogger(getClass()).info("Chosen category: {}", suggestions.category());
                }

                var tagSplit = suggestions.tags()
                    .stream()
                    .collect(Collectors.teeing(
                            Collectors.filtering(tags::contains, Collectors.joining(",")),
                            Collectors.filtering(Predicate.not(tags::contains), Collectors.joining(",")),
                            List::of));

                LoggerFactory.getLogger(getClass()).info("Allowed tags: {}", tagSplit.getFirst());
                LoggerFactory.getLogger(getClass()).warn("Chosen not allowed tags: {}", tagSplit.getLast());
            }
        }
    }

    private void prepareTestData(String language) throws IOException {
        var loader = new Properties();
        try (var configResource = getClass().getResourceAsStream("/" + language + "/test-config.properties")) {
            loader.load(configResource);
        }

        var splitCategories = loader.getProperty("categories", "").split(",");
        this.categories = Arrays.asList(splitCategories);
        var categories = Stream.of(splitCategories)
                .map(c -> Category.builder().label(c.trim()).build())
                .toList();

        var splitTags = loader.getProperty("tags", "").split(",");
        this.tags = Arrays.asList(splitTags);
        var tags = Stream.of(splitTags)
                .map(Tag::new)
                .toList();

        var budget = Budget.builder().build();
        var splitExpenses = loader.getProperty("budgets", "").split(",");
        this.budgets = Arrays.asList(splitExpenses);
        for (var expense : splitExpenses) {
            budget.new Expense(1, expense.trim(), 100);
        }

        Mockito.when(categoryProvider.lookup()).thenReturn(Collections.List(categories));
        Mockito.when(tagProvider.lookup()).thenReturn(Collections.List(tags));
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Control.Option(budget));
    }
}
