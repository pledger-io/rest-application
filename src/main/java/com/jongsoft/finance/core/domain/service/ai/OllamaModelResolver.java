package com.jongsoft.finance.core.domain.service.ai;

import com.jongsoft.finance.configuration.AiConfiguration;

import dev.langchain4j.model.ollama.OllamaModelCard;
import dev.langchain4j.model.ollama.OllamaModels;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

@Singleton
class OllamaModelResolver {
    private static final Logger log = LoggerFactory.getLogger(OllamaModelResolver.class);

    private final AiConfiguration configuration;
    private final OllamaModels ollamaModels;
    private final HttpClient httpClient;

    OllamaModelResolver(AiConfiguration configuration, HttpClient httpClient) {
        this.configuration = configuration;
        ollamaModels = OllamaModels.builder()
                .baseUrl(configuration.getOllama().getUri())
                .build();
        this.httpClient = httpClient;
    }

    OllamaModelCard resolveModel() {
        log.info("Checking for model {}.", configuration.getOllama().getModel());
        if (missesRequiredModel()) {
            fetchModel();
        }

        return ollamaModels.modelCard(configuration.getOllama().getModel()).content();
    }

    private void fetchModel() {
        var modelName = configuration.getOllama().getModel();
        log.info("Model {} is missing, attempting to pull it from Ollama.", modelName);
        var request = HttpRequest.POST(
                configuration.getOllama().getUri() + "/api/pull", Map.of("model", modelName));

        httpClient.toBlocking().exchange(request);
    }

    private boolean missesRequiredModel() {
        return ollamaModels.availableModels().content().stream()
                .noneMatch(model ->
                        Objects.equals(configuration.getOllama().getModel(), model.getModel()));
    }
}
