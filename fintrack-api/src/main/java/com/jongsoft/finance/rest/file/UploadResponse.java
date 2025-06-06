package com.jongsoft.finance.rest.file;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Serializable
record UploadResponse(String fileCode) {}
