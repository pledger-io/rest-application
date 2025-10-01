package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.providers.TagProvider;

import dev.langchain4j.agent.tool.Tool;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.util.List;

@Singleton
@AiEnabled
public class TagClassificationTool implements AiTool {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(TagClassificationTool.class);

    private final TagProvider tagProvider;

    public TagClassificationTool(TagProvider tagProvider) {
        this.tagProvider = tagProvider;
    }

    @Tool(
            """
This tool returns the full list of known tags that can be used when classifying financial transactions.

Use this tool to retrieve or confirm the set of valid tags.
Do not use any tag that is not included in the output of this tool.

Tags provide additional context for classification but are independent of categories and subcategories.""")
    public List<String> listKnownTags() {
        logger.trace("Ai tool fetching available tags.");
        return tagProvider.lookup().map(Tag::name).toJava();
    }
}
