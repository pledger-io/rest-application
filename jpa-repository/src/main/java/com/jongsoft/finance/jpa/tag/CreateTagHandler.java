package com.jongsoft.finance.jpa.tag;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.tag.CreateTagCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class CreateTagHandler implements CommandHandler<CreateTagCommand> {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    @Inject
    public CreateTagHandler(AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateTagCommand command) {
        log.info("[{}] - Processing tag creation event", command.tag());

        var toCreate = TagJpa.builder()
                .name(command.tag())
                .user(entityManager.get(UserAccountJpa.class, Collections.Map("username", authenticationFacade.authenticated())))
                .archived(false)
                .build();

        entityManager.persist(toCreate);
    }

}
