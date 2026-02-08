package com.jongsoft.finance.core.domain.service.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.DisabledChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Factory
@Requires(property = "application.ai.engine", value = "memory")
class InMemorySetup {

    @Bean
    EmbeddingModel inMemoryEmbeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    ChatModel chatModel() {
        return new DisabledChatModel();
    }
}
