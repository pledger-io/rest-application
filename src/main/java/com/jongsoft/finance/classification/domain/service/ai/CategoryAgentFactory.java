package com.jongsoft.finance.classification.domain.service.ai;

import com.jongsoft.finance.classification.domain.service.tools.CategoryTool;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Factory
@Requires(env = "ai")
class CategoryAgentFactory {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CategoryAgentFactory.class);

    @Bean
    CategoryAgent tagAgent(
            ChatModel model, CategoryTool categoryTool, ChatMemoryProvider chatMemoryProvider) {
        log.info("Setting up category chat agent.");
        return AgenticServices.agentBuilder(CategoryAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(categoryTool)
                .name("category-agent")
                .build();
    }
}
