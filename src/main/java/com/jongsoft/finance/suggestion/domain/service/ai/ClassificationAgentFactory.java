package com.jongsoft.finance.suggestion.domain.service.ai;

import com.jongsoft.finance.banking.domain.service.tools.TagTool;
import com.jongsoft.finance.budget.domain.service.ai.BudgetAgent;
import com.jongsoft.finance.classification.domain.service.ai.CategoryAgent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
class ClassificationAgentFactory {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ClassificationAgentFactory.class);

    @Bean
    ClassificationAgent transactionSupportAgent(
            ChatModel model,
            ChatMemoryProvider chatMemoryProvider,
            BudgetAgent budgetAgent,
            CategoryAgent categoryAgent,
            TagTool tagTool) {
        log.info("Setting up transaction support chat agent.");
        return AgenticServices.agentBuilder(ClassificationAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(budgetAgent, categoryAgent, tagTool)
                .build();
    }
}
