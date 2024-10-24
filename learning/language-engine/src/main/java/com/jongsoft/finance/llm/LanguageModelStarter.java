package com.jongsoft.finance.llm;

import com.jongsoft.finance.llm.agent.TransactionSupportAgent;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.jlama.JlamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
@Requires(env = "ai")
public class LanguageModelStarter {
    private static final Logger log = LoggerFactory.getLogger(LanguageModelStarter.class);

    @Bean
    public TransactionSupportAgent transactionSupportAgent(ChatLanguageModel model) {
        log.info("Setting up transaction support chat agent.");
        return AiServices.builder(TransactionSupportAgent.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(chatMemoryProvider())
                .build();
    }

    private ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(3)
                .chatMemoryStore(new InMemoryChatMemoryStore())
                .build();
    }

    @Bean
    @Requires(property = "application.ai.engine", value = "jlama")
    ChatLanguageModel jlamaLanguageModel(@Value("${application.ai.jlama.model}") String modelName, @Value("${application.ai.temperature}") float temperature) {
        log.info("Creating Jlama chat model with name {}, and temperature {}.", modelName, temperature);
        return JlamaChatModel.builder()
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    @Bean
    @Requires(property = "application.ai.engine", value = "open-ai")
    ChatLanguageModel openaiLanguageModel(
            @Value("${application.ai.openai.model}") String modelName,
            @Value("${application.ai.openai.key}") String key,
            @Value("${application.ai.temperature}") double temperature) {
        log.info("Creating OpenAI chat model with name {}, and temperature {}.", modelName, temperature);
        return OpenAiChatModel.builder()
                .modelName(modelName)
                .apiKey(key)
                .temperature(temperature)
                .build();
    }
}
