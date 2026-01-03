package com.jongsoft.finance.core.domain.jpa.entity;

import com.jongsoft.finance.core.domain.WithId;import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "role")
public class RoleJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<UserAccountJpa> users;

    public RoleJpa() {}

    public RoleJpa(Long id, String name, Set<UserAccountJpa> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    @Override public Long getId() {
        return id;
    }

    public String getName() {
    return name;
}@Override
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
