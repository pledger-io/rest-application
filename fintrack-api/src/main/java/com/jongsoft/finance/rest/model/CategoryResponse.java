package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.user.Category;
import io.micronaut.serde.annotation.Serdeable;
import java.time.LocalDate;

@Serdeable.Serializable
public class CategoryResponse {

  private final Category wrapped;

  public CategoryResponse(Category wrapped) {
    this.wrapped = wrapped;
  }

  public long getId() {
    return wrapped.getId();
  }

  public String getLabel() {
    return wrapped.getLabel();
  }

  public String getDescription() {
    return wrapped.getDescription();
  }

  public LocalDate getLastUsed() {
    return wrapped.getLastActivity();
  }
}
