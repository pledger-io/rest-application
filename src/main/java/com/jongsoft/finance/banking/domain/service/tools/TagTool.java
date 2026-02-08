package com.jongsoft.finance.banking.domain.service.tools;

import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.banking.domain.model.Tag;

import dev.langchain4j.agent.tool.Tool;

import io.micronaut.context.annotation.Requires;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.util.List;

@Singleton
@Requires(env = "ai")
public class TagTool {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(TagTool.class);

    private final TagProvider tagProvider;

    public TagTool(TagProvider tagProvider) {
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
