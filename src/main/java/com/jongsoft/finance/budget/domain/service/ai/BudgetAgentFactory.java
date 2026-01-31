package com.jongsoft.finance.budget.domain.service.ai;

import com.jongsoft.finance.budget.domain.service.tools.BudgetTool;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Factory
@Requires(env = "ai")
class BudgetAgentFactory {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(BudgetAgentFactory.class);

    @Bean
    BudgetAgent tagAgent(
            ChatModel model, BudgetTool budgetTool, ChatMemoryProvider chatMemoryProvider) {
        log.info("Setting up budget chat agent.");
        return AgenticServices.agentBuilder(BudgetAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(budgetTool)
                .name("budget-agent")
                .build();
    }
}
