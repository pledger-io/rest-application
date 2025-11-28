package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.RoleJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.CreateUserCommand;

import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;

@Slf4j
@Singleton
@Transactional
public class CreateUserHandler implements CommandHandler<CreateUserCommand> {

    private final ReactiveEntityManager entityManager;
    private final SecretGenerator secretGenerator;

    @Inject
    public CreateUserHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
        secretGenerator = new DefaultSecretGenerator();
    }

    @Override
    @BusinessEventListener
    public void handle(CreateUserCommand command) {
        log.info("[{}] - Processing user create event", command.username());
        var entity = UserAccountJpa.builder()
                .username(command.username())
                .password(command.password())
                .twoFactorSecret(secretGenerator.generate())
                .theme("light")
                .roles(new HashSet<>(Arrays.asList(
                        RoleJpa.builder().id(1L).name("admin").build(),
                        RoleJpa.builder().id(2L).name("accountant").build())))
                .build();

        entityManager.persist(entity);
    }
}
