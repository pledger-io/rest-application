package com.jongsoft.finance.classification.domain.service.ai;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;

public interface CategoryAgent {

    @Agent
    @SystemMessage(
            """
        ## Steps
        - Extract the needed information from the provided transaction description.
        - Use the provided tool to list the available categories.
        - Select at most one category from the list matching the transaction description.
        - Your chosen category *must* be from the list, do not make one up yourself.

        ## Important
        - Only return the chosen category text.""")
    @Tool(
            name = "category-classifier",
            value = """
        Determine the correct category for any given transaction text.""")
    String classifyCategory(String question);
}
