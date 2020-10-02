package com.jongsoft.finance.rest.transaction;

import io.micronaut.core.annotation.Introspected;
import lombok.NoArgsConstructor;

@Introspected
@NoArgsConstructor
class GroupRenameRequest {

    private String name;

    GroupRenameRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
