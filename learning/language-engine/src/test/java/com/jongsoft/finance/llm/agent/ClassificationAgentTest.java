package com.jongsoft.finance.llm.agent;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

@Disabled("Only ran locally to test this new module. Not production ready yet.")
@MicronautTest(environments = "ai")
class ClassificationAgentTest {

    @Inject
    private ClassificationAgent transactionSupportAgent;

    @Inject
    private CategoryProvider categoryProvider;

    @Inject
    private BudgetProvider budgetProvider;

    @Inject
    private TagProvider tagProvider;

    @MockBean
    BudgetProvider budgetProvider() {
        return Mockito.mock(BudgetProvider.class);
    }

    @MockBean
    CategoryProvider categoryProvider() {
        return Mockito.mock(CategoryProvider.class);
    }

    @MockBean
    TagProvider tagProvider() {
        return Mockito.mock(TagProvider.class);
    }

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

                LoggerFactory.getLogger(getClass())
                        .info("Input data: {}", (Object) line);

                var suggestedBudget = transactionSupportAgent.determineBudget(csvReader.getLinesRead(), description, from, to, amount, date);
                LoggerFactory.getLogger(getClass()).info("Chosen budget: {}", suggestedBudget);
                if (!suggestedBudget.isEmpty()) {
                    Assertions.assertThat(suggestedBudget)
                            .as("Budget '%s' expected to be in the provided list.".formatted(suggestedBudget))
                            .isIn(budgets);
                }

                var suggestedCategory = transactionSupportAgent.determineCategory(csvReader.getLinesRead() * 20, description, from, to, amount, date);
                LoggerFactory.getLogger(getClass()).info("Chosen category: {}", suggestedCategory);
                if (!suggestedCategory.isEmpty()) {
                    Assertions.assertThat(suggestedCategory)
                            .as("Category '%s' is expected to be in the provided list.".formatted(suggestedCategory))
                            .isIn(categories);
                }

                var suggestedTags = transactionSupportAgent.determineTags(csvReader.getLinesRead() * 13, description, from, to, amount, date);
                LoggerFactory.getLogger(getClass()).info("Chosen tags: {}", suggestedTags);
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