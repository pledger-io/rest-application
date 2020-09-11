package com.jongsoft.finance.domain.core.events;

import java.io.Serializable;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class CurrencyPropertyEvent<T extends Serializable> implements ApplicationEvent {

    public enum Type {
        ENABLED,
        DECIMAL_PLACES
    }

    private final String code;
    private final T value;
    private final Type type;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param code
     * @param value
     * @param type
     */
    public CurrencyPropertyEvent(Object source, String code, T value, Type type) {
        this.code = code;
        this.value = value;
        this.type = type;
    }

}
