package com.jongsoft.finance.suggestion.domain.service.ai;

import com.jongsoft.finance.AiBase;
import com.jongsoft.finance.banking.domain.model.Tag;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.budget.domain.model.Budget;
import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.suggestion.adapter.api.SuggestionEngine;
import com.jongsoft.finance.suggestion.domain.model.SuggestionInput;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnabledIfEnvironmentVariable(named = "AI_ENGINE", matches = "ollama")
class ClassificationAgentIT extends AiBase {

    @Inject
    private BudgetProvider budgetProvider;

    @Inject
    private SuggestionEngine suggestionEngine;

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
        UserAccount.create("ai-test-nl@local", "password");
        InternalAuthenticationEvent.authenticate("ai-test-nl@local");
        prepareTestData("nl");
        runTestCases("nl");
    }

    private void runTestCases(String language) throws IOException, CsvValidationException {
        try (var testCases = getClass()
                        .getResourceAsStream("/" + language + "/sample-transactions.csv");
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

                var suggestionInput =
                        new SuggestionInput(LocalDate.parse(date), description, from, to, amount);
                var suggestions = suggestionEngine.makeSuggestions(suggestionInput);

                if (!suggestions.budget().isEmpty() && !budgets.contains(suggestions.budget())) {
                    LoggerFactory.getLogger(getClass())
                            .error("Chosen budget not allowed: {}", suggestions.budget());
                } else {
                    LoggerFactory.getLogger(getClass())
                            .info("Chosen budget: {}", suggestions.budget());
                }

                if (!suggestions.category().isEmpty()
                        && !categories.contains(suggestions.category())) {
                    LoggerFactory.getLogger(getClass())
                            .error("Chosen category not allowed: {}", suggestions.category());
                } else {
                    LoggerFactory.getLogger(getClass())
                            .info("Chosen category: {}", suggestions.category());
                }

                var tagSplit = suggestions.tags().stream()
                        .collect(Collectors.teeing(
                                Collectors.filtering(tags::contains, Collectors.joining(",")),
                                Collectors.filtering(
                                        Predicate.not(tags::contains), Collectors.joining(",")),
                                List::of));

                LoggerFactory.getLogger(getClass()).info("Allowed tags: {}", tagSplit.getFirst());
                LoggerFactory.getLogger(getClass())
                        .warn("Chosen not allowed tags: {}", tagSplit.getLast());
            }
        }
    }

    private void prepareTestData(String language) throws IOException {
        var loader = new Properties();
        try (var configResource =
                getClass().getResourceAsStream("/" + language + "/test-config.properties")) {
            loader.load(configResource);
        }

        var splitCategories = loader.getProperty("categories", "").split(",");
        this.categories = Arrays.asList(splitCategories);
        Stream.of(splitCategories).forEach(c -> Category.create(c.trim(), ""));

        var splitTags = loader.getProperty("tags", "").split(",");
        this.tags = Arrays.asList(splitTags);
        Stream.of(splitTags).forEach(Tag::create);

        Budget.create(LocalDate.now().minusMonths(6), 5000);
        var splitExpenses = loader.getProperty("budgets", "").split(",");
        this.budgets = Arrays.asList(splitExpenses);
        var budget = budgetProvider.lookup(Year.now().getValue(), 1).get();
        for (var expense : splitExpenses) {
            budget.createExpense(expense.trim(), 100, 101);
        }
    }
}
