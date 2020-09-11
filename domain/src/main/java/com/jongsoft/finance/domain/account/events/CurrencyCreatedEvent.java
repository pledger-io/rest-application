package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class CurrencyCreatedEvent implements ApplicationEvent {

    private final String name;
    private final char symbol;
    private final String code;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param name
     * @param symbol
     * @param code
     */
    public CurrencyCreatedEvent(Object source, String name, char symbol, String code) {
        this.name = name;
        this.symbol = symbol;
        this.code = code;
    }
}
