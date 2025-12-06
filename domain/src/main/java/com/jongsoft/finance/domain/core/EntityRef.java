package com.jongsoft.finance.domain.core;

import com.jongsoft.finance.domain.Classifier;

import io.micronaut.serde.annotation.Serdeable;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"id"})
public class EntityRef implements Classifier {

    private final Long id;

    public EntityRef(Long id) {
        this.id = id;
    }

    @Serdeable
    public record NamedEntity(long id, String name) implements Classifier {
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
