package com.jongsoft.finance.invoice.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.invoice.adapter.api.InvoiceProvider;
import com.jongsoft.finance.invoice.domain.jpa.entity.InvoiceJpa;
import com.jongsoft.finance.invoice.domain.jpa.mapper.InvoiceMapper;
import com.jongsoft.finance.invoice.domain.model.Invoice;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
class InvoiceProviderJpa implements InvoiceProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final InvoiceMapper invoiceMapper;

    public InvoiceProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            InvoiceMapper invoiceMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.invoiceMapper = invoiceMapper;
    }

    @Override
    public Optional<Invoice> lookup(long id) {
        log.trace("Invoice lookup by id {}.", id);

        return entityManager
                .from(InvoiceJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(invoiceMapper::toDomain);
    }

    @Override
    public Optional<Invoice> lookup(String invoiceNumber) {
        log.trace("Invoice lookup by number {}.", invoiceNumber);

        return entityManager
                .from(InvoiceJpa.class)
                .fieldEq("invoiceNumber", invoiceNumber)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(invoiceMapper::toDomain);
    }
}
