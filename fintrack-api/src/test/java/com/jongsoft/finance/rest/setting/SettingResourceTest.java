package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.core.SettingType;
import com.jongsoft.finance.domain.core.Setting;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.API;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.reactivex.Flowable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class SettingResourceTest {

    private SettingResource subject;

    @Mock
    private SettingProvider settingProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        subject = new SettingResource(settingProvider);

        new EventBus(eventPublisher);
    }

    @Test
    void list() {
        Mockito.when(settingProvider.lookup()).thenReturn(Flowable.just(
                Setting.builder()
                        .name("RecordSetPageSize")
                        .type(SettingType.NUMBER)
                        .value("20")
                        .build(),
                Setting.builder()
                        .name("AutocompleteLimit")
                        .type(SettingType.NUMBER)
                        .value("5")
                        .build()
        ));

        var response = subject.list().test();

        response.assertComplete();
        response.assertValueCount(2);
    }

    @Test
    void update() {
        var setting = Mockito.spy(Setting.builder()
                .name("RecordSetPageSize")
                .value("20")
                .type(SettingType.NUMBER)
                .build());

        Mockito.when(settingProvider.lookup("RecordSetPageSize")).thenReturn(
                API.Option(setting));

        subject.update("RecordSetPageSize", new SettingUpdateRequest("30"));

        Mockito.verify(setting).update("30");
    }
}