package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserProvider;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Collectors;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.SynchronousTransactionManager;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.Currency;

@Singleton
@Transactional
@Named("userProvider")
public class UserProviderJpa extends DataProviderJpa<UserAccount, UserAccountJpa> implements UserProvider {

    private final EntityManager entityManager;

    public UserProviderJpa(
            EntityManager entityManager,
            SynchronousTransactionManager<Connection> transactionManager) {
        super(entityManager, UserAccountJpa.class, transactionManager);
        this.entityManager = entityManager;
    }

    @Override
    public boolean available(String username) {
        var hql = "select count(a.id) from UserAccountJpa a where a.username = :username";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", username);
        long count = singleValue(query);

        return count == 0;
    }

    @Override
    public Optional<UserAccount> lookup(String username) {
        var hql = "select a from UserAccountJpa a where a.username = :username";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", username);
        return API.Option(convert(singleValue(query)));
    }

    @Override
    protected UserAccount convert(UserAccountJpa source) {
        if (source == null) {
            return null;
        }

        return UserAccount.builder()
                .id(source.getId())
                .username(source.getUsername())
                .password(source.getPassword())
                .primaryCurrency(API.Option(source.getCurrency()).getOrSupply(() -> Currency.getInstance("EUR")))
                .secret(source.getTwoFactorSecret())
                .theme(source.getTheme())
                .twoFactorEnabled(source.isTwoFactorEnabled())
                .roles(
                        source.getRoles().stream()
                                .map(role -> new Role(role.getName()))
                                .collect(Collectors.toList()))
                .build();
    }

}
