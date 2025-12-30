package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.providers.CategoryProvider;

import dev.langchain4j.agent.tool.Tool;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.util.List;

@Singleton
@AiEnabled
public class CategoryTool {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(CategoryTool.class);
    private final CategoryProvider categoryProvider;

    CategoryTool(CategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
    }

    @Tool(
            """
This tool returns the full list of known subcategories that can be used when classifying financial transactions.

Use this tool to retrieve or confirm the set of valid subcategories.
Do not use any subcategory that is not included in the output of this tool.

This list contains all subcategories and is independent of any specific category.""")
    public List<String> listKnownSubCategories() {
        logger.trace("Ai tool fetching available categories.");
        return categoryProvider.lookup().map(Category::getLabel).toJava();
    }
}
