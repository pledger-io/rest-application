package com.jongsoft.finance.llm;

import com.jongsoft.finance.llm.agent.ClassificationAgent;
import com.jongsoft.finance.llm.tools.AiTool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Factory
@Requires(env = "ai")
public class LanguageModelStarter {
    private static final Logger log = LoggerFactory.getLogger(LanguageModelStarter.class);

    @Bean
    public ClassificationAgent transactionSupportAgent(ChatLanguageModel model, List<AiTool> aiTools) {
        log.info("Setting up transaction support chat agent.");
        return AiServices.builder(ClassificationAgent.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(aiTools.toArray())
                .build();
    }

    private ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(35)
                .chatMemoryStore(new InMemoryChatMemoryStore())
                .build();
    }

    @Bean
    @Requires(property = "application.ai.engine", value = "ollama")
    ChatLanguageModel ollamaLanguageModel(
            @Value("${application.ai.ollama.model}") String modelName,
            @Value("${application.ai.ollama.uri}") String uri,
            @Value("${application.ai.temperature}") double temperature) {
        log.info("Creating Ollama chat model with name {}, and temperature {}.", modelName, temperature);
        return OllamaChatModel.builder()
                .modelName(modelName)
                .baseUrl(uri)
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
