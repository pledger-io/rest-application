package com.jongsoft.finance.bpmn.delegate.importer;

import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Serdeable
@EqualsAndHashCode(of = {"name"})
public class ExtractionMapping implements Serializable {

    private final String name;
    private final Long accountId;

    public ExtractionMapping(String name, Long accountId) {
        this.name = name;
        this.accountId = accountId;
    }
}
