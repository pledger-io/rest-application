package com.jongsoft.finance.exporter.annotations;

import com.jongsoft.finance.configuration.conditions.ModuleEnabledCondition;

import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Refreshable
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Requires(condition = ModuleEnabledCondition.class, value = "EXPORTER")
public @interface ExporterModuleEnabled {}
