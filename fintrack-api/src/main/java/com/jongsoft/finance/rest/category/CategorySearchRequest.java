package com.jongsoft.finance.rest.category;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Deserializable
public record CategorySearchRequest(int page) {

  public int getPage() {
    return Math.max(page - 1, 0);
  }
}
