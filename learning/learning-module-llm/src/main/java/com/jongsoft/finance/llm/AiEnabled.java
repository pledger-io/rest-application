package com.jongsoft.finance.llm;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Requires(env = "ai")
public @interface AiEnabled {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @interface ClassificationAgent {

    }
}
