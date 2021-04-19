package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.user.events.*;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.user.entity.AccountTokenJpa;
import com.jongsoft.finance.jpa.user.entity.RoleJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import org.jboss.aerogear.security.otp.api.Base32;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Singleton
@Transactional
public class UserEventListener extends RepositoryJpa {

    private final EntityManager entityManager;

    public UserEventListener(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @BusinessEventListener
    public void handleTokenRegistrationEvent(TokenRegisterEvent event) {
        var userAccountJpa = entityManager.createQuery(
                "select u from UserAccountJpa u where u.username = :username",
                UserAccountJpa.class)
                .setParameter("username", event.getUsername())
                .getSingleResult();

        var refreshJpa = AccountTokenJpa.builder()
                .user(userAccountJpa)
                .refreshToken(event.getRefreshToken())
                .expires(event.getExpires())
                .build();

        entityManager.persist(refreshJpa);
    }

    @BusinessEventListener
    public void handleTokenRevokeEvent(TokenRevokeEvent event) {
        var hql = """
                update AccountTokenJpa
                set expires = :now 
                where refreshToken = :token
                    and expires > :now""";

        entityManager.createQuery(hql)
                .setParameter("token", event.getToken())
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
    }

}
