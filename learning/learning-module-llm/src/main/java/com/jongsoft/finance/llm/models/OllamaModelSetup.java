package com.jongsoft.finance.llm.models;

import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.llm.configuration.AiConfiguration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.*;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@Factory
@AiEnabled
@Requires(property = "application.ai.engine", value = "ollama")
class OllamaModelSetup {
    private static final Logger log = LoggerFactory.getLogger(OllamaModelSetup.class);

    private final AiConfiguration configuration;
    private OllamaModelCard chosenModel;

    OllamaModelSetup(AiConfiguration configuration, OllamaModelResolver modelResolver) {
        this.configuration = configuration;
        this.chosenModel = modelResolver.resolveModel();
    }

    @Bean
    ChatModel ollamaLanguageModel() {
        log.info(
                "Creating Ollama chat model with name {}, and temperature {}.",
                configuration.getOllama().getModel(),
                configuration.getTemperature());
        return OllamaChatModel.builder()
                .modelName(configuration.getOllama().getModel())
                .baseUrl(configuration.getOllama().getUri())
                .temperature(configuration.getTemperature())
                .timeout(Duration.ofSeconds(45))
                .build();
    }

    @Bean
    EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(configuration.getOllama().getUri())
                .modelName(configuration.getOllama().getModel())
                .build();
    }

    private boolean noToolSupport() {
        return !chosenModel.getTemplate().contains(".Tools");
    }
}
