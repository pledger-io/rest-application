package com.jongsoft.finance.serialized;

import io.micronaut.serde.annotation.Serdeable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Serdeable
@Getter
@Setter
@EqualsAndHashCode(of = {"name", "iban"})
public final class ExtractedAccountLookup implements Serializable {
  private final String name;
  private final String iban;
  private final String description;

  public ExtractedAccountLookup(String name, String iban, String description) {
    this.name = name;
    this.iban = iban;
    this.description = description;
  }
}
