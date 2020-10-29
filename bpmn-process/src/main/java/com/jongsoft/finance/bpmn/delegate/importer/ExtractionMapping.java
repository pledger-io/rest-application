package com.jongsoft.finance.bpmn.delegate.importer;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;
import java.util.Objects;

@Introspected
public class ExtractionMapping implements Serializable {

    private String name;
    private Long accountId;

    public ExtractionMapping(String name, Long accountId) {
        this.name = name;
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public Long getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExtractionMapping that) {
            return name.equals(that.name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
