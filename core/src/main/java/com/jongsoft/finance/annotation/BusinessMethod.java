package com.jongsoft.finance.annotation;

import io.micronaut.aop.Around;

import java.lang.annotation.*;

/**
 * Methods marked with this annotation contain DDD business logic. These should only be used inside the domain entities.
 */
@Around
@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessMethod {
}
