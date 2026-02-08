package com.jongsoft.finance.banking.domain.service.ai;

import com.jongsoft.finance.banking.domain.service.tools.AccountTool;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Factory
@Requires(env = "ai")
class TransactionExtractorAgentFactory {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TransactionExtractorAgentFactory.class);

    @Bean
    TransactionExtractorAgent createTransactionExtractorAgent(
            ChatModel model, AccountTool accountTool, ChatMemoryProvider chatMemoryProvider) {
        log.info("Setting up tag chat agent.");
        return AiServices.builder(TransactionExtractorAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(accountTool)
                .build();
    }
}
