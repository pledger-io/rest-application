package com.jongsoft.finance.jpa.user.entity;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_account_token")
public class AccountTokenJpa extends EntityJpa {

    @ManyToOne @JoinColumn private UserAccountJpa user;

    @Column(name = "description")
    private String description;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "expires")
    private LocalDateTime expires;

    public AccountTokenJpa() {
        super();
    }

    @Builder
    public AccountTokenJpa(
            Long id,
            UserAccountJpa user,
            String refreshToken,
            LocalDateTime expires,
            String description) {
        super(id);
        this.user = user;
        this.refreshToken = refreshToken;
        this.expires = expires;
        this.description = description;
    }

    @PreUpdate
    @PrePersist
    void initialize() {
        if (created == null) {
            created = LocalDateTime.now();
        }

        if (description == null) {
            description = "Pledger.io Web login";
        }
    }
}
