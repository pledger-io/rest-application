package com.jongsoft.finance.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("application.ai.ollama")
public class OllamaConfiguration {

    private String uri;
    private String model;
    private boolean forceAugmentation;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isForceAugmentation() {
        return forceAugmentation;
    }

    public void setForceAugmentation(boolean forceAugmentation) {
        this.forceAugmentation = forceAugmentation;
    }
}
