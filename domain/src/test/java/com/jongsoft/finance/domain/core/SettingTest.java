package com.jongsoft.finance.domain.core;

import com.jongsoft.finance.core.SettingType;
import com.jongsoft.finance.domain.core.events.SettingUpdatedEvent;
import com.jongsoft.finance.messaging.EventBus;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SettingTest {

    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setup() {
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(eventPublisher);
        new EntityRef(12L);
    }

    @Test
    void update_number() {
        var setting = new Setting("sample_setting", SettingType.NUMBER, "22");

        setting.update("100.2");

        var captor = ArgumentCaptor.forClass(SettingUpdatedEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());
        Assertions.assertThat(captor.getValue().setting()).isEqualTo("sample_setting");
        Assertions.assertThat(captor.getValue().value()).isEqualTo("100.2");
    }

    @Test
    void update_numberInvalid() {
        var setting = new Setting("sample_setting", SettingType.NUMBER, "22");

        var exception = assertThrows(NumberFormatException.class, () -> setting.update("test"));
        Assertions.assertThat(exception).isNotNull();
    }

    @Test
    void update_flag() {
        var setting = new Setting("sample_setting", SettingType.FLAG, "false");

        setting.update("true");

        var captor = ArgumentCaptor.forClass(SettingUpdatedEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());
        Assertions.assertThat(captor.getValue().setting()).isEqualTo("sample_setting");
        Assertions.assertThat(captor.getValue().value()).isEqualTo("true");
    }

    @Test
    void update_flagInvalid() {
        var setting = new Setting("sample_setting", SettingType.FLAG, "false");
        assertThrows(IllegalArgumentException.class, () -> setting.update("test"));
    }


    @Test
    void update_date() {
        var setting = new Setting("sample_setting", SettingType.DATE, "");

        setting.update("2020-02-10");

        var captor = ArgumentCaptor.forClass(SettingUpdatedEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());
        Assertions.assertThat(captor.getValue().setting()).isEqualTo("sample_setting");
        Assertions.assertThat(captor.getValue().value()).isEqualTo("2020-02-10");
    }

    @Test
    void update_dateInvalid() {
        var setting = new Setting("sample_setting", SettingType.DATE, "");
        assertThrows(DateTimeParseException.class, () -> setting.update("test"));
    }
}
