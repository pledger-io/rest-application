package com.jongsoft.finance.jpa.user.entity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.*;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Where;

@Getter
@Entity
@Table(name = "user_account")
public class UserAccountJpa extends EntityJpa {

    @Column(name = "username", unique = true, nullable = false)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;

    private boolean twoFactorEnabled;
    private String twoFactorSecret;

    private String theme;

    private Currency currency;

    @Lob
    @Column
    private byte[] gravatar;

    @ManyToMany
    @JoinTable(name = "user_roles")
    private Set<RoleJpa> roles = new HashSet<>();

    public UserAccountJpa() {
        super();
    }

    @Builder
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

}
