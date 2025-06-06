package com.jongsoft.finance.llm.models;

import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.llm.configuration.AiConfiguration;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import org.slf4j.Logger;

@AiEnabled
@Factory
@Requires(property = "application.ai.engine", value = "open-ai")
public class OpenAISetup {

  private final Logger log = org.slf4j.LoggerFactory.getLogger(OpenAISetup.class);
  private final AiConfiguration configuration;

  public OpenAISetup(AiConfiguration configuration) {
    this.configuration = configuration;
  }

  @Bean
  ChatLanguageModel openaiLanguageModel() {
    log.info(
        "Creating OpenAI chat model with name config: {}.", configuration.getOpenAI().getModel());
    return OpenAiChatModel.builder()
        .modelName(configuration.getOpenAI().getModel())
        .apiKey(configuration.getOpenAI().getKey())
        .temperature(configuration.getTemperature())
        .build();
  }

  @Bean
  EmbeddingModel embeddingModel() {
    return OpenAiEmbeddingModel.builder()
        .modelName(configuration.getOpenAI().getModel())
        .apiKey(configuration.getOpenAI().getKey())
        .build();
  }
}
