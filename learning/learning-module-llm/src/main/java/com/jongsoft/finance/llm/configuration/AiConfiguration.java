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
