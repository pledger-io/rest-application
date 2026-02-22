package com.jongsoft.finance.core.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Introspected
@Table(name = "user_account_token")
public class AccountTokenJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

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

    public AccountTokenJpa(
            Long id,
            UserAccountJpa user,
            String refreshToken,
            LocalDateTime expires,
            String description) {
        this.id = id;
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

    @Override
    public Long getId() {
        return id;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public String getDescription() {
        return description;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getExpires() {
        return expires;
    }
}
