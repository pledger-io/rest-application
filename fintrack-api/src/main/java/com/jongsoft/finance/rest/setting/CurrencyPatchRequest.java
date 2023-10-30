package com.jongsoft.finance.rest.setting;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@Serdeable.Deserializable
public class CurrencyPatchRequest {

    private Integer decimalPlaces;
    private Boolean enabled;

    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    public Boolean getEnabled() {
        return enabled;
    }

}
