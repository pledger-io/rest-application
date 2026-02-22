package com.jongsoft.finance.contract.domain.jpa.handler;

import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.contract.domain.commands.*;
import com.jongsoft.finance.contract.domain.jpa.entity.ContractJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

@Singleton
@Transactional
class ContractChangeHandler {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(ContractChangeHandler.class);
    private final ReactiveEntityManager entityManager;

    ContractChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleCreate(CreateContractCommand command) {
        log.info("[{}] - Processing contract create event", command.name());

        var company = entityManager.getById(AccountJpa.class, command.companyId());

        ContractJpa contract = ContractJpa.create(
                command.name(),
                command.description(),
                command.start(),
                command.end(),
                company,
                company.getUser());

        entityManager.persist(contract);
    }

    @EventListener
    public void handleChange(ChangeContractCommand command) {
        log.info("[{}] - Processing contract changed event", command.id());

        entityManager
                .update(ContractJpa.class)
                .set("name", command.name())
                .set("startDate", command.start())
                .set("endDate", command.end())
                .set("description", command.description())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleWarnBeforeExpire(WarnBeforeExpiryCommand command) {
        log.info("[{}] - Processing contract warning event", command.id());

        entityManager
                .update(ContractJpa.class)
                .set("warningActive", true)
                .set("endDate", command.endDate())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleAttachFile(AttachFileToContractCommand command) {
        log.info("[{}] - Processing contract upload event", command.id());

        entityManager
                .update(ContractJpa.class)
                .set("fileToken", command.fileCode())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleNotificationSend(ContractWarningSend command) {
        log.info("[{}] - Processing contract warning send.", command.contractId());

        entityManager
                .update(ContractJpa.class)
                .set("notificationSend", true)
                .fieldEq("id", command.contractId())
                .execute();
    }

    @EventListener
    public void handleTerminate(TerminateContractCommand command) {
        log.info("[{}] - Processing contract terminate event", command.id());

        entityManager
                .update(ContractJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }
}
