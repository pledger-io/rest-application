package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Deserializable
public class AccountImageRequest {

    private String fileCode;

    private AccountImageRequest() {
        // left blank intentionally for serialization
    }

    public AccountImageRequest(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getFileCode() {
        return fileCode;
    }

}
