package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.AttachFileToContractCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class AttacheFileToContractHandler implements CommandHandler<AttachFileToContractCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public AttacheFileToContractHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(AttachFileToContractCommand command) {
        log.info("[{}] - Processing contract upload event", command.id());

        entityManager.update(ContractJpa.class)
                .set("fileToken", command.fileCode())
                .fieldEq("id", command.id())
                .execute();
    }

}
