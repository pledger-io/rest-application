package com.jongsoft.finance.llm.augmentor;

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
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class ClassificationAugmentor implements RetrievalAugmentor {

    private final Logger logger = getLogger(getClass());

    private final CategoryProvider categoryProvider;
    private final TagProvider tagProvider;
    private final BudgetProvider budgetProvider;

    public ClassificationAugmentor(CategoryProvider categoryProvider, TagProvider tagProvider, BudgetProvider budgetProvider) {
        this.categoryProvider = categoryProvider;
        this.tagProvider = tagProvider;
        this.budgetProvider = budgetProvider;
    }

    @Override
    public AugmentationResult augment(AugmentationRequest augmentationRequest) {
        if (augmentationRequest.chatMessage() instanceof UserMessage userMessage) {
            var messageContents = """
                Select exactly one budget from this list: %s.
                
                %s""".formatted(
                    createBudgetContent(),
                    userMessage.singleText());

            logger.trace("The message for the Ai is as follows: \n{}", messageContents);
            return AugmentationResult.builder()
                    .chatMessage(UserMessage.userMessage(messageContents))
                    .build();
        }

        return RetrievalAugmentor.super.augment(augmentationRequest);
    }

    @Override
    public UserMessage augment(UserMessage userMessage, Metadata metadata) {
        return userMessage;
    }

    private String createCategoryContent() {
        return categoryProvider.lookup()
                .map(Category::getLabel)
                .stream()
                .collect(Collectors.joining(", "));
    }

    private String createTagContent() {
        return tagProvider.lookup()
                .map(Tag::name)
                .stream()
                .collect(Collectors.joining(", "));
    }

    private String createBudgetContent() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        return budgetProvider.lookup(year, month)
                .stream()
                .flatMap(b -> b.getExpenses().stream())
                .map(Budget.Expense::getName)
                .collect(Collectors.joining(", "));
    }
}
