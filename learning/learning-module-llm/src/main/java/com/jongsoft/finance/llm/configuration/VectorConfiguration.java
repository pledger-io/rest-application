package com.jongsoft.finance.llm.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("application.ai.vectors")
public class VectorConfiguration {

    private String passKey;
    private String storage;

    public String getPassKey() {
        return passKey;
    }

    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}
