package com.jongsoft.finance.llm.models;

import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.llm.ToolSupplier;
import com.jongsoft.finance.llm.augmenters.ClassificationAugmenter;
import com.jongsoft.finance.llm.configuration.AiConfiguration;
import com.jongsoft.finance.llm.tools.AiTool;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.TagProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaModelCard;
import dev.langchain4j.model.ollama.OllamaModels;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
@AiEnabled
@Requires(property = "application.ai.engine", value = "ollama")
class OllamaModelSetup {
  private static final Logger log = LoggerFactory.getLogger(OllamaModelSetup.class);

  private final AiConfiguration configuration;
  private OllamaModelCard chosenModel;

  OllamaModelSetup(AiConfiguration configuration) {
    this.configuration = configuration;
    retrieveModelInfo();
  }

  @Bean
  ToolSupplier toolSupplier(List<AiTool> knownTools) {
    if (configuration.getOllama().isForceAugmentation() || noToolSupport()) {
      return () -> new Object[0];
    }

    log.debug("Setting up Ai tools to be used with Ollama.");
    return () -> knownTools.toArray(new AiTool[0]);
  }

  @Bean
  @AiEnabled.ClassificationAgent
  RetrievalAugmentor classificationAugmenter(
      BudgetProvider budgetProvider, CategoryProvider categoryProvider, TagProvider tagProvider) {
    if (configuration.getOllama().isForceAugmentation() || noToolSupport()) {
      log.debug("Creating a classification augmenter since tools are not supported.");
      return new ClassificationAugmenter(budgetProvider, categoryProvider, tagProvider);
    }

    return (userMessage) ->
        AugmentationResult.builder().chatMessage(userMessage.chatMessage()).build();
  }

  @Bean
  ChatLanguageModel ollamaLanguageModel() {
    log.info(
        "Creating Ollama chat model with name {}, and temperature {}.",
        configuration.getOllama().getModel(),
        configuration.getTemperature());
    return OllamaChatModel.builder()
        .modelName(configuration.getOllama().getModel())
        .baseUrl(configuration.getOllama().getUri())
        .temperature(configuration.getTemperature())
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

  private void retrieveModelInfo() {
    var modelsResponse =
        OllamaModels.builder()
            .baseUrl(configuration.getOllama().getUri())
            .build()
            .modelCard(configuration.getOllama().getModel());

    chosenModel = modelsResponse.content();
  }
}
