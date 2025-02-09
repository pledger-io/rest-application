package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.importer.entity.ImportConfig;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.importer.CreateConfigurationCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class CreateConfigurationHandler implements CommandHandler<CreateConfigurationCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public CreateConfigurationHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateConfigurationCommand command) {
        log.info("[{}] - Processing CSV configuration create event", command.name());

        var entity = ImportConfig.builder()
                .fileCode(command.fileCode())
                .name(command.name())
                .type(command.type())
                .user(entityManager.currentUser())
                .build();

        entityManager.persist(entity);
    }

}
