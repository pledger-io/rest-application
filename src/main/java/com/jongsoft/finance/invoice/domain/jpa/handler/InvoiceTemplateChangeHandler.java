package com.jongsoft.finance.invoice.domain.jpa.handler;

import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.invoice.domain.commands.CreateInvoiceTemplateCommand;
import com.jongsoft.finance.invoice.domain.commands.UpdateInvoiceTemplateCommand;
import com.jongsoft.finance.invoice.domain.jpa.entity.InvoiceTemplateJpa;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class InvoiceTemplateChangeHandler {
    private final Logger log = LoggerFactory.getLogger(InvoiceTemplateChangeHandler.class);

    private final ReactiveEntityManager entityManager;

    InvoiceTemplateChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleCreateTemplate(CreateInvoiceTemplateCommand command) {
        log.info("[{}] - Processing invoice template create event", command.name());

        var toCreate = InvoiceTemplateJpa.of(
                command.name(),
                command.headerContent(),
                command.footerContent(),
                entityManager.currentUser());

        entityManager.persist(toCreate);
    }

    @EventListener
    public void handleUpdateTemplate(UpdateInvoiceTemplateCommand command) {
        log.info("[{}] - Processing invoice template update event", command.id());

        entityManager
                .update(InvoiceTemplateJpa.class)
                .set("name", command.name())
                .set("headerContent", command.headerContent())
                .set("footerContent", command.footerContent())
                .fieldEq("id", command.id())
                .execute();
    }
}
