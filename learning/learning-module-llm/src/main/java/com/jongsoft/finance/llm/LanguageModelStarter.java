package com.jongsoft.finance.llm;

import com.jongsoft.finance.llm.agent.*;
import com.jongsoft.finance.llm.tools.AccountTool;
import com.jongsoft.finance.llm.tools.BudgetTool;
import com.jongsoft.finance.llm.tools.CategoryTool;
import com.jongsoft.finance.llm.tools.TagTool;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Factory
@AiEnabled
class LanguageModelStarter {
    private static final Logger log = LoggerFactory.getLogger(LanguageModelStarter.class);

    @Bean
    public TransactionExtractorAgent transactionExtractorAgent(
            ChatModel model, AccountTool accountTool) {
        log.info("Setting up transaction extractor chat agent.");

        return AiServices.builder(TransactionExtractorAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(accountTool)
                .build();
    }

    @Bean
    @AiEnabled.AiExecutor
    public ExecutorService executorService() {
        return Executors.newScheduledThreadPool(5);
    }

}
