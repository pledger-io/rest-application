package com.jongsoft.finance.jpa.tag;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "tags")
public class TagJpa extends EntityJpa {

    private String name;
    private boolean archived;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private UserAccountJpa user;

    @Builder
    private TagJpa(String name, boolean archived, UserAccountJpa user) {
        this.name = name;
        this.archived = archived;
        this.user = user;
    }

    protected TagJpa() {
    }
}
