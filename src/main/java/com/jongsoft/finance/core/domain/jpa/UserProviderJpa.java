package com.jongsoft.finance.core.domain.jpa;

import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.jpa.entity.AccountTokenJpa;
import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.domain.jpa.mapper.UserAccountMapper;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.core.domain.model.SessionToken;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.support.Collections;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Singleton
@ReadOnly
public class UserProviderJpa implements UserProvider {

    private final Logger logger;
    private final ReactiveEntityManager entityManager;
    private final UserAccountMapper userAccountMapper;

    @Inject
    public UserProviderJpa(
            ReactiveEntityManager entityManager, UserAccountMapper userAccountMapper) {
        this.entityManager = entityManager;
        this.userAccountMapper = userAccountMapper;
        this.logger = LoggerFactory.getLogger(UserProvider.class);
    }

    @Override
    public Sequence<UserAccount> lookup() {
        return entityManager.from(UserAccountJpa.class).joinFetch("roles").stream()
                .map(userAccountMapper::toDomain)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
    }

    @Override
    public Optional<UserAccount> lookup(long id) {
        return entityManager
                .from(UserAccountJpa.class)
                .joinFetch("roles")
                .fieldEq("id", id)
                .singleResult()
                .map(userAccountMapper::toDomain);
    }

    @Override
    public boolean available(UserIdentifier username) {
        return entityManager
                        .from(UserAccountJpa.class)
                        .fieldEq("username", username.email())
                        .projectSingleValue(Long.class, "count(1)")
                        .getOrSupply(() -> 0L)
                == 0;
    }

    @Override
    public Optional<UserAccount> lookup(UserIdentifier username) {
        logger.debug("Locating user {} in the system.", username.email());
        return entityManager
                .from(UserAccountJpa.class)
                .fieldEq("username", username.email())
                .singleResult()
                .map(userAccountMapper::toDomain);
    }

    @Override
    public Optional<UserAccount> refreshToken(String refreshToken) {
        return entityManager
                .from(AccountTokenJpa.class)
                .fieldEq("refreshToken", refreshToken)
                .fieldGtOrEq("expires", LocalDateTime.now())
                .projectSingleValue(UserAccountJpa.class, "e.user")
                .map(userAccountMapper::toDomain);
    }

    @Override
    public Sequence<SessionToken> tokens(UserIdentifier username) {
        return entityManager
                .from(AccountTokenJpa.class)
                .fieldEq("user.username", username.email())
                .fieldGtOrEq("expires", LocalDateTime.now())
                .stream()
                .map(userAccountMapper::toDomain)
                .collect(Collections.collector(com.jongsoft.lang.Collections::List));
    }
}
