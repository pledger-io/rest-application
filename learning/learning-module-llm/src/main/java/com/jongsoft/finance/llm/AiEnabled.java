package com.jongsoft.finance.llm;

import io.micronaut.context.annotation.Requires;

@Requires(env = "ai")
public @interface AiEnabled {
}
