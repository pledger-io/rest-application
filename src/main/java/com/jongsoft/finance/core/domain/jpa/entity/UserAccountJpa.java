package com.jongsoft.finance.core.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

@Entity
@Introspected
@Table(name = "user_account")
public class UserAccountJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    private String theme;

    private Currency currency;

    @Lob
    @Column
    private byte[] gravatar;

    @JoinTable(name = "user_roles")
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<RoleJpa> roles = new HashSet<>();

    public UserAccountJpa() {}

    private UserAccountJpa(
            String username,
            String password,
            boolean twoFactorEnabled,
            String twoFactorSecret,
            String theme,
            Currency currency,
            byte[] gravatar,
            Set<RoleJpa> roles) {
        this.username = username;
        this.password = password;
        this.twoFactorEnabled = twoFactorEnabled;
        this.twoFactorSecret = twoFactorSecret;
        this.theme = theme;
        this.currency = currency;
        this.gravatar = gravatar;
        this.roles = roles;
    }

    public static UserAccountJpa of(
            String username,
            String password,
            String twoFactorSecret,
            String theme,
            Currency currency,
            Set<RoleJpa> roles) {
        return new UserAccountJpa(
                username, password, false, twoFactorSecret, theme, currency, null, roles);
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public String getTheme() {
        return theme;
    }

    public Currency getCurrency() {
        return currency;
    }

    public byte[] getGravatar() {
        return gravatar;
    }

    public Set<RoleJpa> getRoles() {
        return roles;
    }
}
