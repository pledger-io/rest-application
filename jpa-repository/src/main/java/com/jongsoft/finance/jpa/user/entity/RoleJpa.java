package com.jongsoft.finance.jpa.user.entity;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "role")
public class RoleJpa extends EntityJpa {

  private String name;

  @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
  private Set<UserAccountJpa> users;

  public RoleJpa() {}

  @Builder
  public RoleJpa(Long id, String name, Set<UserAccountJpa> users) {
    super(id);
    this.name = name;
    this.users = users;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof RoleJpa other) {
      return Objects.equals(other.name, name);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), name);
  }
}
