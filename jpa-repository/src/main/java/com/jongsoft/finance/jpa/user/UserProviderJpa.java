package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserProvider;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Collectors;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Maybe;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Currency;

@Singleton
@Transactional
@Named("userProvider")
public class UserProviderJpa implements UserProvider {

    private final ReactiveEntityManager entityManager;

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
    public boolean available(String username) {
        return entityManager.<Long>blocking()
                .hql("select count(a.id) from UserAccountJpa a where a.username = :username")
                .set("username", username)
                .maybe()
                .getOrSupply(() -> 0L) == 0;
    }

    @Override
    public Optional<UserAccount> lookup(String username) {
        return entityManager.<UserAccountJpa>blocking()
                .hql("from UserAccountJpa a where a.username = :username")
                .set("username", username)
                .maybe()
                .map(this::convert);
    }

    @Override
    public Maybe<UserAccount> refreshToken(String refreshToken) {
        var hql = """
                select u.user from AccountTokenJpa u
                where u.refreshToken = :refreshToken
                    and u.expires > :now""";

        return entityManager.<UserAccountJpa>reactive()
                .hql(hql)
                .set("refreshToken", refreshToken)
                .set("now", LocalDateTime.now())
                .maybe()
                .map(this::convert);
    }

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
