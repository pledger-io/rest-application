package com.jongsoft.finance.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.micronaut.runtime.event.annotation.EventListener;

/**
 * The business event listener can be used to annotate a method handling an application events that were originally triggered
 * by an {@link BusinessMethod}.
 *
 * @see BusinessMethod
 */
@Documented
@EventListener
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface BusinessEventListener {
}
