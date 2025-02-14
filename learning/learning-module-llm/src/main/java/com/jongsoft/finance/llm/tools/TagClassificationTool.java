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

    @Tool("Returns a list of known tags")
    public List<String> listKnownTags() {
        logger.debug("Ai tool fetching available tags.");
        return tagProvider.lookup()
                .map(Tag::name)
                .toJava();
    }
}
