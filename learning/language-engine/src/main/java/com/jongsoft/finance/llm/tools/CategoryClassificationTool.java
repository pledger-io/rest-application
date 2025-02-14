package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import dev.langchain4j.agent.tool.Tool;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.util.List;

@Singleton
@Requires(env = "ai")
public class CategoryClassificationTool implements AiTool {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(CategoryClassificationTool.class);
    private final CategoryProvider categoryProvider;

    CategoryClassificationTool(CategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
    }

    @Tool("Returns a list of known categories, when classifying pick from this list only")
    public List<String> listKnownCategories() {
        logger.debug("Ai tool fetching available categories.");
        return categoryProvider.lookup()
                .map(Category::getLabel)
                .toJava();
    }
}
