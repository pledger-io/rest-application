package com.jongsoft.finance.invoice.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.invoice.adapter.api.InvoiceTemplateProvider;
import com.jongsoft.finance.invoice.domain.jpa.entity.InvoiceTemplateJpa;
import com.jongsoft.finance.invoice.domain.jpa.mapper.InvoiceTemplateMapper;
import com.jongsoft.finance.invoice.domain.model.InvoiceTemplate;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
class InvoiceTemplateProviderJpa implements InvoiceTemplateProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final InvoiceTemplateMapper templateMapper;

    public InvoiceTemplateProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            InvoiceTemplateMapper templateMapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.templateMapper = templateMapper;
    }

    @Override
    public Optional<InvoiceTemplate> lookup(long id) {
        log.trace("InvoiceTemplate lookup by id {}.", id);

        return entityManager
                .from(InvoiceTemplateJpa.class)
                .fieldEq("id", id)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(templateMapper::toDomain);
    }

    @Override
    public Optional<InvoiceTemplate> lookup(String name) {
        log.trace("InvoiceTemplate lookup by name {}.", name);

        return entityManager
                .from(InvoiceTemplateJpa.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(templateMapper::toDomain);
    }
}
