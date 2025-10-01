package com.jongsoft.finance;

/**
 * Classes implementing the {@link SupportIndicating} interface can be asked what type of Entity
 * they expose.
 *
 * @param <T> the entity type
 */
public interface SupportIndicating<T> {

    boolean supports(Class<?> supportingClass);
}
