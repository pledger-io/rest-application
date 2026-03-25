package com.jongsoft.finance.project.domain.model;

import com.jongsoft.finance.core.value.Identifier;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record ClientIdentifier(Long id) implements Identifier {}
