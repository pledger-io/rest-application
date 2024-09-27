package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable.Deserializable
record AccountImageRequest(@Schema(description = "The file code that was returned after the upload") String fileCode) {
}
