package com.jongsoft.finance.core.domain.service.ai;

import com.jongsoft.finance.configuration.AiConfiguration;

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
@Requires(env = "ai")
@Requires(property = "application.ai.engine", value = "ollama")
class OllamaModelSetup {
    private static final Logger log = LoggerFactory.getLogger(OllamaModelSetup.class);

    private final AiConfiguration configuration;

    OllamaModelSetup(AiConfiguration configuration, OllamaModelResolver modelResolver) {
        this.configuration = configuration;
        var chosenModel = modelResolver.resolveModel();
        log.info(
                "Resolved Ollama model: {}, {}",
                chosenModel.getDetails().getFamily(),
                chosenModel.getCapabilities());
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
                .modelName(configuration.getOllama().getEmbeddingModel())
                .build();
    }
}
