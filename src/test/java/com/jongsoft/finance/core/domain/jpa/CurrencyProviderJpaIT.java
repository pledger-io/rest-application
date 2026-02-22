package com.jongsoft.finance.core.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.core.adapter.api.CurrencyProvider;
import com.jongsoft.finance.core.domain.commands.ChangeCurrencyPropertyCommand;
import com.jongsoft.finance.core.domain.commands.CreateCurrencyCommand;
import com.jongsoft.finance.core.domain.commands.CurrencyCommandType;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Currencies")
public class CurrencyProviderJpaIT extends JpaTestSetup {

    @Inject
    private CurrencyProvider currencyProvider;

    @BeforeEach
    void init() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql");
    }

    @Test
    @DisplayName("Check currency lookup")
    void lookup_all() {
        var check = currencyProvider.lookup();

        Assertions.assertThat(check).hasSize(3);
    }

    @Test
    @DisplayName("Check currency lookup - EUR")
    void lookup_eur() {
        var check = currencyProvider.lookup("EUR").get();

        Assertions.assertThat(check.getCode()).isEqualTo("EUR");
        Assertions.assertThat(check.getName()).isEqualTo("Euro");
    }

    @Test
    @DisplayName("Create currency")
    void handleCreate() {
        var check = currencyProvider.lookup("MST");
        Assertions.assertThat(check).isEmpty();

        CreateCurrencyCommand.currencyCreated("Mistral", 'D', "MST");

        var currency = currencyProvider.lookup("MST").get();
        Assertions.assertThat(currency.getName()).isEqualTo("Mistral");
        Assertions.assertThat(currency.getCode()).isEqualTo("MST");
        Assertions.assertThat(currency.isEnabled()).isTrue();
        Assertions.assertThat(currency.getDecimalPlaces()).isEqualTo(2);
    }

    @Test
    @DisplayName("Change currency decimal places")
    void handleProperty_decimalPlaces() {
        ChangeCurrencyPropertyCommand.currencyPropertyChanged(
                "EUR", 12, CurrencyCommandType.DECIMAL_PLACES);

        var check = currencyProvider.lookup("EUR").get();
        Assertions.assertThat(check.getDecimalPlaces()).isEqualTo(12);
    }

    @Test
    @DisplayName("Change currency enabled")
    void handleProperty_enable() {
        ChangeCurrencyPropertyCommand.currencyPropertyChanged(
                "EUR", false, CurrencyCommandType.ENABLED);

        Assertions.assertThat(currencyProvider.lookup("EUR"))
                .first()
                .satisfies(
                        currency -> Assertions.assertThat(currency.isEnabled()).isFalse());
    }
}
