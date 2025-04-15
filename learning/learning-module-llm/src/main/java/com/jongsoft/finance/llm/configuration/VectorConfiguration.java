package com.jongsoft.finance.llm.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("application.ai.vectors")
public class VectorConfiguration {

    public enum StorageType {
        MEMORY,
        PGSQL
    }

    private StorageType storageType;
    private String passKey;
    private String storage;

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public StorageType getStorageType() {
        return storageType;
    }

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
