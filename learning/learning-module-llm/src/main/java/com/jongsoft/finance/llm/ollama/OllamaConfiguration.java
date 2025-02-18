package com.jongsoft.finance.llm.ollama;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * The configuration properties for the Ollama LLM agent.
 *
 * @param uri   The URI to the LLM server
 * @param model The model to be used, this must be already installed
 * @param forceAugmentation Should augmentation be forced over the usage of tools. This is especially useful for smaller models.
 */
@ConfigurationProperties("application.ai.ollama")
record OllamaConfiguration(String uri, String model, boolean forceAugmentation) {
}
