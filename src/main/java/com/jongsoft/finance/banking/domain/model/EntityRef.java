package com.jongsoft.finance.banking.domain.model;

import com.jongsoft.finance.core.value.WithId;

import io.micronaut.serde.annotation.Serdeable;

public class EntityRef implements Classifier {

    private final Long id;

    public EntityRef(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Serdeable
    public record NamedEntity(long id, String name) implements Classifier, WithId {
        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
