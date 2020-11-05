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
import java.util.*;

@Singleton
@Transactional
public class UserEventListener extends RepositoryJpa {

    private final EntityManager entityManager;

    public UserEventListener(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @BusinessEventListener
    public void handleUserAccountCreatedEvent(UserAccountCreatedEvent event) {
        var entity = UserAccountJpa.builder()
                .username(event.getUsername())
                .password(event.getPassword())
                .twoFactorSecret(Base32.random())
                .theme("dark")
                .roles(new HashSet<>(Arrays.asList(
                        RoleJpa.builder().id(1L).name("admin").build(),
                        RoleJpa.builder().id(2L).name("accountant").build())))
                .build();

        entityManager.persist(entity);
    }

    @BusinessEventListener
    public void handleUserAccountPasswordEvent(UserAccountPasswordChangedEvent event) {
        var hql = """
                update UserAccountJpa 
                set password = :password
                where username = :username""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", event.getUsername());
        query.setParameter("password", event.getPassword());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleUserAccountMultifactorEvent(UserAccountMultiFactorEvent event) {
        var hql = """
                update UserAccountJpa 
                set twoFactorEnabled = :enabled
                where username = :username""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", event.getUsername());
        query.setParameter("enabled", event.isEnabled());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleUserAccountSettingEvent(UserAccountSettingEvent event) {
        var hql = "update UserAccountJpa set ";

        Map<String, Object> params = new HashMap<>(1);
        switch (event.getTypeOfSetting()) {
            case THEME -> {
                hql += " theme = :theme";
                params.put("theme", event.getValue());
            }
            case CURRENCY -> {
                hql += " currency = :currency";
                params.put("currency", Currency.getInstance(event.getValue()));
            }
            default -> {

            }
        }
        hql += " where username = :username";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", event.getUsername());
        params.forEach(query::setParameter);
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleTokenRegistrationEvent(TokenRegisteredEvent event) {
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

}
