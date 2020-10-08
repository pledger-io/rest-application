package com.jongsoft.finance.rest;

import io.micronaut.core.convert.format.Format;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Format("yyyy-MM-dd")
public @interface DateFormat {
}
