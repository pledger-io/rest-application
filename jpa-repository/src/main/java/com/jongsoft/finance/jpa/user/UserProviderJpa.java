package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.AccountTokenJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.collection.Collectors;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;
import java.util.Currency;

@Singleton
@ReadOnly
@RequiresJpa
@Named("userProvider")
public class UserProviderJpa implements UserProvider {

    private final ReactiveEntityManager entityManager;

    @Inject
    public UserProviderJpa(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<UserAccount> lookup(long id) {
        return entityManager.<UserAccountJpa>blocking()
                .hql("from UserAccountJpa a where a.id = :id")
                .set("id", id)
                .maybe()
                .map(this::convert);
    }

    @Override
    public boolean available(UserIdentifier username) {
        return entityManager.<Long>blocking()
                .hql("select count(a.id) from UserAccountJpa a where a.username = :username")
                .set("username", username.email())
                .maybe()
                .getOrSupply(() -> 0L) == 0;
    }

    @Override
    public Optional<UserAccount> lookup(UserIdentifier username) {
        return entityManager.<UserAccountJpa>blocking()
                .hql("from UserAccountJpa a where a.username = :username")
                .set("username", username.email())
                .maybe()
                .map(this::convert);
    }

    @Override
    public Optional<UserAccount> refreshToken(String refreshToken) {
        var hql = """
                select u.user from AccountTokenJpa u
                where u.refreshToken = :refreshToken
                    and u.expires >= :now""";

        return entityManager.<UserAccountJpa>blocking()
                .hql(hql)
                .set("refreshToken", refreshToken)
                .set("now", LocalDateTime.now())
                .maybe()
                .map(this::convert);
    }

    @Override
    public Sequence<SessionToken> tokens(UserIdentifier username) {
        var hql = """
                from AccountTokenJpa
                where user.username = :username
                      and expires > :now""";

        return entityManager.<AccountTokenJpa>blocking()
                .hql(hql)
                .set("username", username.email())
                .set("now", LocalDateTime.now())
                .sequence()
                .map(this::convert);
    }

    protected SessionToken convert(AccountTokenJpa source) {
        return SessionToken.builder()
                .id(source.getId())
                .description(source.getDescription())
                .token(source.getRefreshToken())
                .validity(Dates.range(
                        source.getCreated(),
                        source.getExpires()))
                .build();
    }

    protected UserAccount convert(UserAccountJpa source) {
        if (source == null) {
            return null;
        }

        return UserAccount.builder()
                .id(source.getId())
                .username(new UserIdentifier(source.getUsername()))
                .password(source.getPassword())
                .primaryCurrency(Control.Option(source.getCurrency()).getOrSupply(() -> Currency.getInstance("EUR")))
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
