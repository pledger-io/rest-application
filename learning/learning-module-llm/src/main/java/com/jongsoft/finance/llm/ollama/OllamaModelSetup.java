package com.jongsoft.finance.llm.ollama;

import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.llm.ToolSupplier;
import com.jongsoft.finance.llm.augmenters.ClassificationAugmenter;
import com.jongsoft.finance.llm.tools.AiTool;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.TagProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaModelCard;
import dev.langchain4j.model.ollama.OllamaModels;
import dev.langchain4j.rag.RetrievalAugmentor;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Factory
@AiEnabled
@Requires(property = "application.ai.engine", value = "ollama")
class OllamaModelSetup {
    private static final Logger log = LoggerFactory.getLogger(OllamaModelSetup.class);

    private final OllamaConfiguration configuration;
    private OllamaModelCard chosenModel;

    OllamaModelSetup(OllamaConfiguration configuration) {
        this.configuration = configuration;
        retrieveModelInfo();
    }

    @Bean
    ToolSupplier toolSupplier(List<AiTool> knownTools) {
        if (configuration.forceAugmentation() || noToolSupport()) {
            return () -> new Object[0];
        }

        log.debug("Setting up Ai tools to be used with Ollama.");
        return () -> knownTools.toArray(new AiTool[0]);
    }

    @Bean
    @AiEnabled.ClassificationAgent
    RetrievalAugmentor classificationAugmenter(BudgetProvider budgetProvider, CategoryProvider categoryProvider, TagProvider tagProvider) {
        if (configuration.forceAugmentation() || noToolSupport()) {
            log.debug("Creating a classification augmenter since tools are not supported.");
            return new ClassificationAugmenter(budgetProvider, categoryProvider, tagProvider);
        }

        return null;
    }

    @Bean
    ChatLanguageModel ollamaLanguageModel(@Value("${application.ai.temperature}") double temperature) {
        log.info("Creating Ollama chat model with name {}, and temperature {}.", configuration.model(), temperature);
        return OllamaChatModel.builder()
                .modelName(configuration.model())
                .baseUrl(configuration.uri())
                .temperature(temperature)
                .build();
    }

    private boolean noToolSupport() {
        return !chosenModel.getTemplate().contains(".Tools");
    }

    private void retrieveModelInfo() {
        var modelsResponse = OllamaModels.builder()
                .baseUrl(configuration.uri())
                .build()
                .modelCard(configuration.model());

        chosenModel = modelsResponse.content();
    }
}
