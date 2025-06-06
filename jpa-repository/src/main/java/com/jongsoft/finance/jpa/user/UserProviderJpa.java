package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.AccountTokenJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.collection.Collectors;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.support.Collections;
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
  public Sequence<UserAccount> lookup() {
    return entityManager.from(UserAccountJpa.class).joinFetch("roles").stream()
        .map(this::convert)
        .collect(Collections.collector(com.jongsoft.lang.Collections::List));
  }

  @Override
  public Optional<UserAccount> lookup(long id) {
    return entityManager
        .from(UserAccountJpa.class)
        .joinFetch("roles")
        .fieldEq("id", id)
        .singleResult()
        .map(this::convert);
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
    return entityManager
        .from(UserAccountJpa.class)
        .fieldEq("username", username.email())
        .singleResult()
        .map(this::convert);
  }

  @Override
  public Optional<UserAccount> refreshToken(String refreshToken) {
    return entityManager
        .from(AccountTokenJpa.class)
        .fieldEq("refreshToken", refreshToken)
        .fieldGtOrEq("expires", LocalDateTime.now())
        .projectSingleValue(UserAccountJpa.class, "e.user")
        .map(this::convert);
  }

  @Override
  public Sequence<SessionToken> tokens(UserIdentifier username) {
    return entityManager
        .from(AccountTokenJpa.class)
        .fieldEq("user.username", username.email())
        .fieldGtOrEq("expires", LocalDateTime.now())
        .stream()
        .map(this::convert)
        .collect(Collections.collector(com.jongsoft.lang.Collections::List));
  }

  protected SessionToken convert(AccountTokenJpa source) {
    return SessionToken.builder()
        .id(source.getId())
        .description(source.getDescription())
        .token(source.getRefreshToken())
        .validity(Dates.range(source.getCreated(), source.getExpires()))
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
        .primaryCurrency(
            Control.Option(source.getCurrency()).getOrSupply(() -> Currency.getInstance("EUR")))
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
