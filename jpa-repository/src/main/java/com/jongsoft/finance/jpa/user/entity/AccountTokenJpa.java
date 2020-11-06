package com.jongsoft.finance.jpa.user.entity;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_account_token")
public class AccountTokenJpa extends EntityJpa {

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expires")
    private LocalDateTime expires;

    public AccountTokenJpa() {
        super();
    }

    @Builder
    public AccountTokenJpa(Long id, UserAccountJpa user, String refreshToken, LocalDateTime expires) {
        super(id);
        this.user = user;
        this.refreshToken = refreshToken;
        this.expires = expires;
    }

}
