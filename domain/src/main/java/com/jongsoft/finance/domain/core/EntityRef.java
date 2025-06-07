package com.jongsoft.finance.domain.core;

import com.jongsoft.finance.core.AggregateBase;
import io.micronaut.serde.annotation.Serdeable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"id"})
public class EntityRef implements AggregateBase {

  private final Long id;

  public EntityRef(Long id) {
    this.id = id;
  }

  @Serdeable
  public record NamedEntity(long id, String name) implements AggregateBase {
    @Override
    public Long getId() {
      return id;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
