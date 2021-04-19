package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.importer.entity.CSVImportConfig;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.importer.CreateConfigurationCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class CreateConfigurationHandler implements CommandHandler<CreateConfigurationCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public CreateConfigurationHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateConfigurationCommand command) {
        log.trace("[{}] - Processing CSV configuration create event", command.name());

        var entity = CSVImportConfig.builder()
                .fileCode(command.fileCode())
                .name(command.name())
                .user(entityManager.get(UserAccountJpa.class, Collections.Map("username", authenticationFacade.authenticated())))
                .build();

        entityManager.persist(entity);
    }

}
