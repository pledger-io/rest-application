package com.jongsoft.finance.llm.junit;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AiTestRunExtension.class)
@MicronautTest(environments = "ai")
@EnabledIfEnvironmentVariable(named = "AI_ENABLED", matches = "true")
public @interface AiTestRun {
}
