package com.jongsoft.finance.banking.service.ai;

import com.jongsoft.finance.banking.service.tools.TagTool;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Factory
@Requires(env = "ai")
class TagAgentFactory {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TagAgentFactory.class);

    @Bean
    TagAgent tagAgent(ChatModel model, TagTool tagTool, ChatMemoryProvider chatMemoryProvider) {
        log.info("Setting up tag chat agent.");
        return AgenticServices.agentBuilder(TagAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(tagTool)
                .name("tag-agent")
                .build();
    }
}
