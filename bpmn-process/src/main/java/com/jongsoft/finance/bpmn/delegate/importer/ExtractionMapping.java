package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessVariable;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Serdeable
@EqualsAndHashCode(of = {"name"})
public class ExtractionMapping implements ProcessVariable {

    private final String name;
    private final Long accountId;

    public ExtractionMapping(String name, Long accountId) {
        this.name = name;
        this.accountId = accountId;
    }
}
