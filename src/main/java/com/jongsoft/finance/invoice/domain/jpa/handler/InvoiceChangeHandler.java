package com.jongsoft.finance.invoice.domain.jpa.handler;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.invoice.domain.commands.*;
import com.jongsoft.finance.invoice.domain.jpa.entity.InvoiceJpa;
import com.jongsoft.finance.invoice.domain.jpa.entity.InvoiceLineJpa;
import com.jongsoft.finance.invoice.domain.jpa.entity.InvoiceTemplateJpa;
import com.jongsoft.finance.invoice.domain.jpa.entity.TaxBracketJpa;
import com.jongsoft.finance.project.domain.jpa.entity.ClientJpa;
import com.jongsoft.finance.project.domain.jpa.entity.TimeEntryJpa;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class InvoiceChangeHandler {
    private final Logger log = LoggerFactory.getLogger(InvoiceChangeHandler.class);

    private final ReactiveEntityManager entityManager;

    InvoiceChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleCreateInvoice(CreateInvoiceCommand command) {
        log.info("[{}] - Processing invoice create event", command.invoiceNumber());

        var client = entityManager
                .from(ClientJpa.class)
                .fieldEq("id", command.clientId())
                .singleResult()
                .get();

        var template = entityManager
                .from(InvoiceTemplateJpa.class)
                .fieldEq("id", command.templateId())
                .singleResult()
                .get();

        var toCreate = InvoiceJpa.of(
                command.invoiceNumber(),
                client,
                command.invoiceDate(),
                command.dueDate(),
                template,
                entityManager.currentUser());

        entityManager.persist(toCreate);
    }

    @EventListener
    public void handleUpdateInvoice(UpdateInvoiceCommand command) {
        log.info("[{}] - Processing invoice update event", command.id());

        entityManager
                .update(InvoiceJpa.class)
                .set("invoiceNumber", command.invoiceNumber())
                .set("invoiceDate", command.invoiceDate())
                .set("dueDate", command.dueDate())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleAddLine(AddInvoiceLineCommand command) {
        log.info(
                "[{}] - Processing invoice line add event for invoice {}",
                command.description(),
                command.invoiceId());

        var invoice = entityManager
                .from(InvoiceJpa.class)
                .fieldEq("id", command.invoiceId())
                .singleResult()
                .get();

        var taxBracket = entityManager
                .from(TaxBracketJpa.class)
                .fieldEq("id", command.taxBracketId())
                .singleResult()
                .get();

        var line = InvoiceLineJpa.of(
                invoice,
                command.description(),
                command.quantity(),
                command.unit(),
                command.unitPrice(),
                taxBracket);

        // Attach time entries if provided
        if (command.timeEntryIds() != null && !command.timeEntryIds().isEmpty()) {
            for (Long timeEntryId : command.timeEntryIds()) {
                entityManager
                        .from(TimeEntryJpa.class)
                        .fieldEq("id", timeEntryId)
                        .singleResult()
                        .ifPresent(timeEntry -> {
                            if (!timeEntry.getProject().isBillable()) {
                                throw StatusException.badRequest(
                                        "Time entries from non-billable projects cannot be invoiced.",
                                        "project.not.billable");
                            }
                            line.getTimeEntries().add(timeEntry);
                            timeEntry.setInvoiced(true);
                        });
            }
        }

        entityManager.persist(line);
    }

    @EventListener
    public void handleAttachTimeEntry(AttachTimeEntryToLineCommand command) {
        log.info(
                "[{}] - Processing time entry attach event to line {}",
                command.timeEntryId(),
                command.invoiceLineId());

        var line = entityManager
                .from(InvoiceLineJpa.class)
                .fieldEq("id", command.invoiceLineId())
                .singleResult()
                .get();

        var timeEntry = entityManager
                .from(TimeEntryJpa.class)
                .fieldEq("id", command.timeEntryId())
                .singleResult()
                .get();

        if (!timeEntry.getProject().isBillable()) {
            throw StatusException.badRequest(
                    "Time entries from non-billable projects cannot be invoiced.",
                    "project.not.billable");
        }

        if (!line.getTimeEntries().contains(timeEntry)) {
            line.getTimeEntries().add(timeEntry);
            timeEntry.setInvoiced(true);
        }
    }

    @EventListener
    public void handleFinalizeInvoice(FinalizeInvoiceCommand command) {
        log.info("[{}] - Processing invoice finalize event", command.id());

        var invoice = entityManager
                .from(InvoiceJpa.class)
                .fieldEq("id", command.id())
                .singleResult()
                .get();

        invoice.setFinalized(true);
        invoice.setPdfToken(command.pdfToken());

        // Mark all time entries as invoiced
        invoice.getLines().forEach(line -> line.getTimeEntries()
                .forEach(timeEntry -> timeEntry.setInvoiced(true)));
    }
}
