package com.jongsoft.finance.domain.core;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.domain.account.events.CurrencyCreatedEvent;
import com.jongsoft.finance.domain.core.events.CurrencyPropertyEvent;
import com.jongsoft.finance.domain.core.events.CurrencyRenameEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.Control;
import lombok.*;

@Getter
@Builder
@Aggregate
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Currency implements AggregateBase {

    private Long id;
    private String name;
    private String code;
    private char symbol;
    private int decimalPlaces;
    private boolean enabled;

    public Currency(String name, String code, char symbol) {
        this.name = name;
        this.code = code;
        this.symbol = symbol;
        this.decimalPlaces = 2;
        this.enabled = true;

        EventBus.getBus().send(
                new CurrencyCreatedEvent(
                        this,
                        name,
                        symbol,
                        code
                )
        );
    }

    @BusinessMethod
    public void rename(String name, String code, char symbol) {
        var changed = Control.Equal(this.name, name)
                .append(this.code, code)
                .append(this.symbol, symbol)
                .isNotEqual();

        if (changed) {
            this.name = name;
            this.code = code;
            this.symbol = symbol;

            EventBus.getBus().send(
                    new CurrencyRenameEvent(
                            this,
                            id,
                            name,
                            symbol,
                            code
                    )
            );
        }
    }

    @BusinessMethod
    public void disable() {
        if (enabled) {
            enabled = false;

            EventBus.getBus().send(
                    new CurrencyPropertyEvent<>(
                            this,
                            code,
                            false,
                            CurrencyPropertyEvent.Type.ENABLED
                    )
            );
        }
    }

    @BusinessMethod
    public void enable() {
        if (!enabled) {
            enabled = true;

            EventBus.getBus().send(
                    new CurrencyPropertyEvent<>(
                            this,
                            code,
                            true,
                            CurrencyPropertyEvent.Type.ENABLED
                    )
            );
        }
    }

    @BusinessMethod
    public void accuracy(int decimalPlaces) {
        if (decimalPlaces != this.decimalPlaces) {
            this.decimalPlaces = decimalPlaces;


            EventBus.getBus().send(
                    new CurrencyPropertyEvent<>(
                            this,
                            code,
                            decimalPlaces,
                            CurrencyPropertyEvent.Type.DECIMAL_PLACES
                    )
            );
        }
    }
}
