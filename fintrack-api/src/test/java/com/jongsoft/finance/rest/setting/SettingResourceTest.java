package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.core.SettingType;
import com.jongsoft.finance.domain.core.Setting;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Setting resource")
class SettingResourceTest extends TestSetup {

    @Inject
    private SettingProvider settingProvider;

    @Replaces
    @MockBean
    SettingProvider settingProvider() {
        return Mockito.mock(SettingProvider.class);
    }

    @Test
    @DisplayName("Should return setting by name")
    void list(RequestSpecification spec) {
        Mockito.when(settingProvider.lookup()).thenReturn(Collections.List(
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

        // @formatter:off
        spec
            .when()
                .get("/api/settings")
            .then()
                .statusCode(200)
                .body("name", Matchers.hasItems("RecordSetPageSize", "AutocompleteLimit"));
        // @formatter:on
    }

    @Test
    @DisplayName("Update setting by name")
    void update(RequestSpecification spec) {
        var setting = Mockito.spy(Setting.builder()
                .name("RecordSetPageSize")
                .value("20")
                .type(SettingType.NUMBER)
                .build());

        Mockito.when(settingProvider.lookup("RecordSetPageSize")).thenReturn(
                Control.Option(setting));

        // @formatter:off
        spec
            .given()
                .body(new SettingUpdateRequest("30"))
            .when()
                .post("/api/settings/RecordSetPageSize")
            .then()
                .statusCode(200);
        // @formatter:on

        Mockito.verify(setting).update("30");
    }
}
