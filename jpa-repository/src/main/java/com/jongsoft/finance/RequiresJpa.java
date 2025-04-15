package com.jongsoft.finance;

import io.micronaut.context.annotation.Requires;

@Requires(env = {"h2", "mysql", "demo", "psql"})
public @interface RequiresJpa {
}
