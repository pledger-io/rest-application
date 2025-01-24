package com.jongsoft.finance.domain.core;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.messaging.commands.currency.ChangeCurrencyPropertyCommand;
import com.jongsoft.finance.messaging.commands.currency.CreateCurrencyCommand;
import com.jongsoft.finance.messaging.commands.currency.CurrencyCommandType;
import com.jongsoft.finance.messaging.commands.currency.RenameCurrencyCommand;
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

        CreateCurrencyCommand.currencyCreated(name, symbol, code);
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
            RenameCurrencyCommand.currencyRenamed(id, name, symbol, code);
        }
    }

    @BusinessMethod
    public void disable() {
        if (enabled) {
            enabled = false;
            ChangeCurrencyPropertyCommand.currencyPropertyChanged(code, false, CurrencyCommandType.ENABLED);
        }
    }

    @BusinessMethod
    public void enable() {
        if (!enabled) {
            enabled = true;
            ChangeCurrencyPropertyCommand.currencyPropertyChanged(code, true, CurrencyCommandType.ENABLED);
        }
    }

    @BusinessMethod
    public void accuracy(int decimalPlaces) {
        if (decimalPlaces != this.decimalPlaces) {
            this.decimalPlaces = decimalPlaces;
            ChangeCurrencyPropertyCommand.currencyPropertyChanged(code, decimalPlaces, CurrencyCommandType.DECIMAL_PLACES);
        }
    }
}
