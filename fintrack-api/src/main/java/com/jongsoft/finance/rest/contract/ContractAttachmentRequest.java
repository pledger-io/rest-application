package com.jongsoft.finance.rest.contract;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Setter;

@Setter
@Serdeable.Deserializable
public class ContractAttachmentRequest {

    private String fileCode;

    public String getFileCode() {
        return fileCode;
    }

}
