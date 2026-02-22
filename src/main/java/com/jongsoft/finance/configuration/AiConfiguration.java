package com.jongsoft.finance.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

@Requires(env = "ai")
@ConfigurationProperties("application.ai")
public class AiConfiguration {

    private String engine;
    private Double temperature;
    private final OllamaConfiguration ollama;
    private final OpenAiConfiguration openAI;

    AiConfiguration(OllamaConfiguration ollama, OpenAiConfiguration openAI) {
        this.ollama = ollama;
        this.openAI = openAI;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public OllamaConfiguration getOllama() {
        return ollama;
    }

    public OpenAiConfiguration getOpenAI() {
        return openAI;
    }
}
