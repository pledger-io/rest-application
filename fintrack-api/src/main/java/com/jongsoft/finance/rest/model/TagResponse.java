package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.transaction.Tag;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class TagResponse {

    private final Tag wrapped;

    public TagResponse(Tag wrapped) {
        this.wrapped = wrapped;
    }

    public String getName() {
        return wrapped.name();
    }
}
