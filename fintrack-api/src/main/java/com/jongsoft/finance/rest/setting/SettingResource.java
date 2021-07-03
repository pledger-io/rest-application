package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.model.SettingResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;

@Secured("admin")
@Controller("/api/settings")
@Tag(name = "Application Settings")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SettingResource {

    private final SettingProvider settingProvider;

    @Get
    @Operation(
            summary = "Get settings",
            description = "List all available settings in the system",
            operationId = "getSettings"
    )
    @Secured({SecurityRule.IS_ANONYMOUS, SecurityRule.IS_AUTHENTICATED})
    Flowable<SettingResponse> list() {
        return settingProvider.lookup()
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
    @ApiResponse(responseCode = "204")
    void update(@PathVariable String setting, @Body SettingUpdateRequest request) {
        settingProvider.lookup(setting)
                .ifPresent(value -> value.update(request.getValue()));
    }

}
