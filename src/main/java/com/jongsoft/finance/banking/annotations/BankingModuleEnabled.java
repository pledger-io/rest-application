package com.jongsoft.finance.banking.annotations;

import com.jongsoft.finance.configuration.conditions.ModuleEnabledCondition;

import io.micronaut.context.annotation.Requires;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Requires(condition = ModuleEnabledCondition.class, value = "BANKING")
public @interface BankingModuleEnabled {}
