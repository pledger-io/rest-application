package com.jongsoft.finance.banking.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

@Entity
@Introspected
@Table(name = "tags")
public class TagJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private boolean archived;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private UserAccountJpa user;

    public TagJpa(String name, boolean archived, UserAccountJpa user) {
        this.name = name;
        this.archived = archived;
        this.user = user;
    }

    protected TagJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isArchived() {
        return archived;
    }

    public UserAccountJpa getUser() {
        return user;
    }
}
