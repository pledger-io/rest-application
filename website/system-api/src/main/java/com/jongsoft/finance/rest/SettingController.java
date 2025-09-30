package com.jongsoft.finance.rest;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.Setting;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.model.SettingRequest;
import com.jongsoft.finance.rest.model.SettingResponse;
import com.jongsoft.finance.rest.model.SettingResponseType;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import jakarta.validation.Valid;
import java.util.List;

@Controller
public class SettingController implements SettingsApi {

  private final SettingProvider settingProvider;

  public SettingController(SettingProvider settingProvider) {
    this.settingProvider = settingProvider;
  }

  @Override
  public List<@Valid SettingResponse> getAllSettings() {
    return settingProvider.lookup().map(this::convert).toJava();
  }

  @Override
  public HttpResponse<Void> patchSetting(String setting, SettingRequest settingRequest) {
    var existing =
        settingProvider
            .lookup(setting)
            .getOrThrow(() -> StatusException.notFound("No setting found with name " + setting));

    existing.update(settingRequest.getValue().toString());
    return HttpResponse.noContent();
  }

  private SettingResponse convert(Setting setting) {
    var settingType =
        switch (setting.getType()) {
          case DATE -> SettingResponseType.DATE;
          case FLAG -> SettingResponseType.FLAG;
          case NUMBER -> SettingResponseType.NUMBER;
          case STRING -> SettingResponseType.STRING;
        };

    return new SettingResponse(setting.getName(), setting.getValue(), settingType);
  }
}
