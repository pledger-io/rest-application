package com.jongsoft.finance.llm.augmenters;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.TagProvider;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.query.Metadata;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A class that implements the RetrievalAugmentor interface to provide additional classification augmentation capabilities for chat messages.
 * It retrieves information from BudgetProvider, CategoryProvider, and TagProvider to augment the user messages based on certain conditions.
 */
public class ClassificationAugmenter implements RetrievalAugmentor {
    private final Logger logger = getLogger(getClass());

    private final BudgetProvider budgetProvider;
    private final CategoryProvider categoryProvider;
    private final TagProvider tagProvider;

    public ClassificationAugmenter(BudgetProvider budgetProvider, CategoryProvider categoryProvider, TagProvider tagProvider) {
        this.budgetProvider = budgetProvider;
        this.categoryProvider = categoryProvider;
        this.tagProvider = tagProvider;
    }

    @Override
    public AugmentationResult augment(AugmentationRequest augmentationRequest) {
        if (augmentationRequest.chatMessage() instanceof UserMessage userMessage) {
            return AugmentationResult.builder()
                    .chatMessage(augment(userMessage, augmentationRequest.metadata()))
                    .build();
        }

        throw new IllegalStateException("Could not augment a message of type " + augmentationRequest.chatMessage().getClass().getName());
    }

    @Override
    public UserMessage augment(UserMessage userMessage, Metadata metadata) {
        var currentMessage = userMessage.singleText();

        String allowedList = "";
        if (currentMessage.contains("Pick the correct category for a transaction on")) {
            logger.trace("User message augmentation with available budgets.");
            int year = LocalDate.now().getYear();
            int month = LocalDate.now().getMonthValue();

            allowedList = budgetProvider.lookup(year, month)
                    .stream()
                    .flatMap(b -> b.getExpenses().stream())
                    .map(Budget.Expense::getName)
                    .collect(Collectors.joining(","));
        }

        if (currentMessage.contains("Pick the correct subcategory for a transaction")) {
            logger.trace("User message augmentation with available categories.");
            allowedList = categoryProvider.lookup()
                    .map(Category::getLabel)
                    .stream()
                    .collect(Collectors.joining(","));
        }

        if (currentMessage.contains("Pick the correct tags for a transaction on")) {
            logger.trace("User message augmentation with available tags.");
            allowedList = tagProvider.lookup()
                    .map(Tag::name)
                    .stream()
                    .collect(Collectors.joining(","));
        }

        var updatedRequest = """
                %s
                You must choose from the following options: [%s].
                
                Your response must **only** contain the chosen option in plain text and nothing else. Do not add any explanation, formatting, or extra words.""".formatted(
                currentMessage.split("\n")[0],
                allowedList);


        return UserMessage.userMessage(updatedRequest);
    }
}
