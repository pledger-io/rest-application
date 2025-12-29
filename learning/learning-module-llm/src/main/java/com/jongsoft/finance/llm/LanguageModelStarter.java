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
    BudgetAgent budgetAgent(ChatModel model, BudgetTool budgetTool) {
        log.info("Setting up budget chat agent.");
        return AgenticServices.agentBuilder(BudgetAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(budgetTool)
                .name("budget-agent")
                .build();
    }

    @Bean
    CategoryAgent categoryAgent(ChatModel model, CategoryTool categoryTool) {
        log.info("Setting up category chat agent.");
        return AgenticServices.agentBuilder(CategoryAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(categoryTool)
                .name("category-agent")
                .build();
    }

    @Bean
    TagAgent tagAgent(ChatModel model, TagTool tagTool) {
        log.info("Setting up tag chat agent.");
        return AgenticServices.agentBuilder(TagAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(tagTool)
                .name("tag-agent")
                .build();
    }

    @Bean
    public ClassificationAgent transactionSupportAgent(
            ChatModel model,
            BudgetAgent budgetAgent,
            CategoryAgent categoryAgent,
            TagTool tagTool) {
        log.info("Setting up transaction support chat agent.");
        return AgenticServices.agentBuilder(ClassificationAgent.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(budgetAgent, categoryAgent, tagTool)
                .build();
    }

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

    private ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(new InMemoryChatMemoryStore())
                .build();
    }
}
