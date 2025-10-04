package com.jongsoft.finance.rest.setting;

import static com.jongsoft.finance.rest.ApiConstants.TAG_SETTINGS;

import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.model.SettingResponse;
import com.jongsoft.finance.security.AuthenticationRoles;

import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Secured(AuthenticationRoles.IS_ADMIN)
@Controller("/api/settings")
@Tag(name = TAG_SETTINGS)
public class SettingResource {

    private final SettingProvider settingProvider;

    public SettingResource(SettingProvider settingProvider) {
        this.settingProvider = settingProvider;
    }

    @Get
    @Operation(
            summary = "Get settings",
            description = "List all available settings in the system",
            operationId = "getSettings")
    @Secured({SecurityRule.IS_ANONYMOUS, SecurityRule.IS_AUTHENTICATED})
    List<SettingResponse> list() {
        return settingProvider
                .lookup()
                .map(setting -> new SettingResponse(
                        setting.getName(), setting.getValue(), setting.getType()))
                .toJava();
    }

    @Post("/{setting}")
    @Operation(
            summary = "Update setting",
            description = "Update a single setting in the system",
            operationId = "updateSettings")
    @ApiResponse(responseCode = "204")
    void update(@PathVariable String setting, @Body SettingUpdateRequest request) {
        settingProvider.lookup(setting).ifPresent(value -> value.update(request.value()));
    }
}
