package com.jongsoft.finance.rest.setting;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor
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
