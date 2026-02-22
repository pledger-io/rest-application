package com.jongsoft.finance.classification.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

@Entity
@Introspected
@Table(name = "category")
public class CategoryJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String label;
    private String description;
    private boolean archived;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private UserAccountJpa user;

    public CategoryJpa(String label, String description, UserAccountJpa user) {
        this.label = label;
        this.description = description;
        this.user = user;
    }

    protected CategoryJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isArchived() {
        return archived;
    }

    public UserAccountJpa getUser() {
        return user;
    }
}
