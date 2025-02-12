package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.llm.AITool;
import com.jongsoft.finance.providers.CategoryProvider;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class CategoryTool implements AITool {

    private static final Logger logger = getLogger(CategoryTool.class);

    private final CategoryProvider categoryProvider;

    public CategoryTool(CategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
    }

    @Tool("Returns the known categories")
    public List<String> getAvailableCategories() {
        logger.info("Fetching available categories.");

        return categoryProvider.lookup()
                .map(Category::getLabel)
                .toJava();
    }

}
