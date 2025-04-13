package com.jongsoft.finance.llm.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("application.ai.vectors")
public class VectorConfiguration {

    private String passKey;
    private String classificationStore;

    public String getPassKey() {
        return passKey;
    }

    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }

    public String getClassificationStore() {
        return classificationStore;
    }

    public void setClassificationStore(String classificationStore) {
        this.classificationStore = classificationStore;
    }
}
