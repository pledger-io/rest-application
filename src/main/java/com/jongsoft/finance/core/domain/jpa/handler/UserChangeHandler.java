package com.jongsoft.finance.core.domain.jpa.handler;

import com.jongsoft.finance.core.domain.commands.*;
import com.jongsoft.finance.core.domain.jpa.entity.RoleJpa;
import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.lang.control.Optional;

import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Transactional
class UserChangeHandler {
    private static final List<String> ROLES = List.of("admin", "accountant");

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserChangeHandler.class);
    private final ReactiveEntityManager entityManager;
    private final SecretGenerator secretGenerator;

    UserChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
        secretGenerator = new DefaultSecretGenerator();
    }

    @EventListener
    public void handleCreateExternalUser(CreateExternalUserCommand command) {
        if (validateUserExists(command.username())) {
            log.debug(
                    "[{}] - External user already exists, skipping creation.", command.username());
            return;
        }

        log.info("[{}] - Creating external user", command.username());

        Set<RoleJpa> roles = command.roles().stream()
                .map(role ->
                        entityManager.from(RoleJpa.class).fieldEq("name", role).singleResult())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        UserAccountJpa entity = UserAccountJpa.of(
                command.username(),
                "",
                secretGenerator.generate(),
                "light",
                Currency.getInstance("EUR"),
                roles);

        entityManager.persist(entity);
        UserCreatedCommand.userCreated(command.username());
    }

    @EventListener
    public void handleCreateUser(CreateUserCommand command) {
        log.info("[{}] - Processing user create event", command.username());

        Set<RoleJpa> roles = ROLES.stream()
                .map(role ->
                        entityManager.from(RoleJpa.class).fieldEq("name", role).singleResult())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        UserAccountJpa entity = UserAccountJpa.of(
                command.username(),
                command.password(),
                secretGenerator.generate(),
                "light",
                Currency.getInstance("EUR"),
                roles);

        entityManager.persist(entity);
        InternalAuthenticationEvent.authenticate(command.username());
        UserCreatedCommand.userCreated(command.username());
    }

    @EventListener
    public void handleMultiFactorChange(ChangeMultiFactorCommand command) {
        log.info("[{}] - Updating multi factor setting", command.username());

        entityManager
                .update(UserAccountJpa.class)
                .set("twoFactorEnabled", command.enabled())
                .fieldEq("username", command.username().email())
                .execute();
    }

    @EventListener
    public void handlePasswordChange(ChangePasswordCommand command) {
        log.info("[{}] - Updating password for user", command.username());

        entityManager
                .update(UserAccountJpa.class)
                .set("password", command.password())
                .fieldEq("username", command.username().email())
                .execute();
    }

    @EventListener
    public void handleSettingChange(ChangeUserSettingCommand command) {
        log.info("[{}] - Updating user setting {}", command.username(), command.type());

        var query = entityManager
                .update(UserAccountJpa.class)
                .fieldEq("username", command.username().email());

        switch (command.type()) {
            case THEME -> query.set("theme", command.value());
            case CURRENCY -> query.set("currency", Currency.getInstance(command.value()));
            default -> {}
        }
        query.execute();
    }

    private synchronized boolean validateUserExists(String username) {
        return entityManager
                .from(UserAccountJpa.class)
                .fieldEq("username", username)
                .singleResult()
                .isPresent();
    }
}
