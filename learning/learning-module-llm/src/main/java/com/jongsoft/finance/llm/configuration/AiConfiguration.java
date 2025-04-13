package com.jongsoft.finance.llm.configuration;

import com.jongsoft.finance.llm.AiEnabled;
import io.micronaut.context.annotation.ConfigurationProperties;

@AiEnabled
@ConfigurationProperties("application.ai")
public class AiConfiguration {

    private String engine;
    private Double temperature;
    private final OllamaConfiguration ollama;
    private final OpenAiConfiguration openAI;
    private final VectorConfiguration vectors;

    AiConfiguration(OllamaConfiguration ollama, OpenAiConfiguration openAI, VectorConfiguration vectors) {
        this.ollama = ollama;
        this.openAI = openAI;
        this.vectors = vectors;
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

    public VectorConfiguration getVectors() {
        return vectors;
    }
}
