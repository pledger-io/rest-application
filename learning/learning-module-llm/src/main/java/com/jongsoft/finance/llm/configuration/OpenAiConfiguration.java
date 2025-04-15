package com.jongsoft.finance.llm.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("application.ai.openai")
public class OpenAiConfiguration {

    private String key;
    private String model;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
