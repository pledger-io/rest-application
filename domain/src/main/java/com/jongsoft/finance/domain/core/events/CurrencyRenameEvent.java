package com.jongsoft.finance.domain.core.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class CurrencyRenameEvent implements ApplicationEvent {

    private final long id;
    private final String name;
    private final char symbol;
    private final String code;

    /**
     * Create a new {@code ApplicationEvent}.
     *  @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param id
     * @param name
     * @param symbol
     * @param code
     */
    public CurrencyRenameEvent(Object source, long id, String name, char symbol, String code) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.code = code;
    }
}
