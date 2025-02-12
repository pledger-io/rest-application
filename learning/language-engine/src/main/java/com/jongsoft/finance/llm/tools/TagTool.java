package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.llm.AITool;
import com.jongsoft.finance.providers.TagProvider;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class TagTool implements AITool {

    private static final Logger logger = getLogger(TagTool.class);

    private final TagProvider tagProvider;

    public TagTool(TagProvider tagProvider) {
        this.tagProvider = tagProvider;
    }

    @Tool("Returns the known tags")
    public List<String> getAvailableTags() {
        logger.info("Fetching available tags");

        return tagProvider.lookup()
                .map(Tag::name)
                .toJava();
    }

}
