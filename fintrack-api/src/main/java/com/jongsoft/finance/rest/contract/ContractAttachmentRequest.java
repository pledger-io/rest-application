package com.jongsoft.finance.rest.contract;

import io.micronaut.core.annotation.Introspected;
import lombok.Setter;

@Setter
@Introspected
public class ContractAttachmentRequest {

    private String fileCode;

    public String getFileCode() {
        return fileCode;
    }

}
