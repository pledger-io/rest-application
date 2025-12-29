package com.jongsoft.finance.llm.agent;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;

import java.util.List;

public interface TagAgent {

    @Agent
    @SystemMessage(
            """
        ## Steps
        - Extract the needed information from the provided transaction description.
        - Use the provided tool to list the available tags.
        - Select at most one or more tags from the list matching the transaction description.
        - Your chosen tag *must* be from the list, do not make one up yourself.

        ## Important
        - Only return the chosen tag text.""")
    @Tool(
            name = "tag-classifier",
            value = """
        Determine the correct tags for any given transaction text.""")
    List<String> classifyTags(String question);
}
