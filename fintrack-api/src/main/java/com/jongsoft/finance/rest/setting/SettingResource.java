package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.rest.model.SettingResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.reactivex.Flowable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Secured("ADMIN")
@Controller("/api/settings")
@Tag(name = "Application Settings")
public class SettingResource {

    private final SettingProvider settingProvider;

    public SettingResource(SettingProvider settingProvider) {
        this.settingProvider = settingProvider;
    }

    @Get
    @Operation(
            summary = "Get settings",
            description = "List all available settings in the system",
            operationId = "getSettings"
    )
    Flowable<SettingResponse> list() {
        return Flowable.fromIterable(settingProvider.lookup())
                .map(setting -> new SettingResponse(
                        setting.getName(),
                        setting.getValue(),
                        setting.getType()));
    }

    @Post("/{setting}")
    @Operation(
            summary = "Update setting",
            description = "Update a single setting in the system",
            operationId = "updateSettings"
    )
    void update(@PathVariable String setting, @Body SettingUpdateRequest request) {
        settingProvider.lookup(setting)
                .ifPresent(value -> value.update(request.getValue()));
    }

}
