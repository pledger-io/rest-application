package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.RoleJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.CreateExternalUserCommand;

import dev.samstevens.totp.secret.SecretGenerator;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;

@Singleton
@Transactional
class CreateExternalUserHandler implements CommandHandler<CreateExternalUserCommand> {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private final ReactiveEntityManager entityManager;
    private final SecretGenerator secretGenerator;

    public CreateExternalUserHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
        secretGenerator = new dev.samstevens.totp.secret.DefaultSecretGenerator();
    }

    @Override
    @BusinessEventListener
    public void handle(CreateExternalUserCommand command) {
        if (validateUserExists(command.username())) {
            log.debug(
                    "[{}] - External user already exists, skipping creation.", command.username());
            return;
        }

        log.info("[{}] - Creating external user", command.username());
        var builder =
                UserAccountJpa.builder()
                        .username(command.username())
                        .password("")
                        .theme("light")
                        .twoFactorSecret(secretGenerator.generate());

        for (var role : command.roles()) {
            entityManager
                    .from(RoleJpa.class)
                    .fieldEq("name", role)
                    .singleResult()
                    .ifPresent(builder::role);
        }

        entityManager.persist(builder.build());
    }

    private synchronized boolean validateUserExists(String username) {
        return entityManager
                .from(UserAccountJpa.class)
                .fieldEq("username", username)
                .singleResult()
                .isPresent();
    }
}
