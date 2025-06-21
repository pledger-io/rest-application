package com.jongsoft.finance.llm;

import com.jongsoft.finance.llm.agent.ClassificationAgent;
import com.jongsoft.finance.llm.agent.TransactionExtractorAgent;
import com.jongsoft.finance.llm.tools.AiTool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
@AiEnabled
class LanguageModelStarter {
  private static final Logger log = LoggerFactory.getLogger(LanguageModelStarter.class);

  @Bean
  public ClassificationAgent transactionSupportAgent(
      ChatLanguageModel model,
      ToolSupplier aiTools,
      @AiEnabled.ClassificationAgent @Nullable RetrievalAugmentor retrievalAugmentor) {
    log.info("Setting up transaction support chat agent.");
    var aiBuilder = AiServices.builder(ClassificationAgent.class)
        .chatLanguageModel(model)
        .chatMemoryProvider(chatMemoryProvider());

    if (aiTools.getTools().length > 0) {
      aiBuilder.tools(aiTools.getTools());
    }
    Optional.ofNullable(retrievalAugmentor).ifPresent(aiBuilder::retrievalAugmentor);

    return aiBuilder.build();
  }

  @Bean
  public TransactionExtractorAgent transactionExtractorAgent(
      ChatLanguageModel model, ToolSupplier aiTools) {
    log.info("Setting up transaction extractor chat agent.");
    var aiBuilder = AiServices.builder(TransactionExtractorAgent.class)
        .chatLanguageModel(model)
        .chatMemoryProvider(chatMemoryProvider());

    if (aiTools.getTools().length > 0) {
      aiBuilder.tools(aiTools.getTools());
    }

    return aiBuilder.build();
  }

  @Bean
  @AiEnabled.AiExecutor
  public ExecutorService executorService() {
    return Executors.newScheduledThreadPool(5);
  }

  private ChatMemoryProvider chatMemoryProvider() {
    return memoryId -> MessageWindowChatMemory.builder()
        .id(memoryId)
        .maxMessages(10)
        .chatMemoryStore(new InMemoryChatMemoryStore())
        .build();
  }

  @Bean
  @AiEnabled.ClassificationAgent
  @Requires(property = "application.ai.engine", value = "open-ai")
  Optional<RetrievalAugmentor> classificationAugmenter() {
    return Optional.empty();
  }

  @Bean
  @Requires(property = "application.ai.engine", value = "open-ai")
  ToolSupplier toolSupplier(List<AiTool> knownTools) {
    return () -> knownTools.toArray(new AiTool[0]);
  }
}
