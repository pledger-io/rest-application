package com.jongsoft.finance.exporter.domain.jpa;

import com.jongsoft.finance.banking.adapter.api.LinkableProvider;
import com.jongsoft.finance.banking.types.TransactionLinkType;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.exporter.adapter.api.ImportProvider;
import com.jongsoft.finance.exporter.domain.jpa.entity.ImportJpa;
import com.jongsoft.finance.exporter.domain.jpa.mapper.BatchImportMapper;
import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
public class ImportProviderJpa implements ImportProvider, LinkableProvider<BatchImport> {

    private final Logger log = LoggerFactory.getLogger(ImportProviderJpa.class);

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final BatchImportMapper mapper;

    @Inject
    public ImportProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            BatchImportMapper mapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.mapper = mapper;
    }

    @Override
    public Sequence<BatchImport> lookup() {
        return entityManager
                .from(ImportJpa.class)
                .fieldEq("archived", false)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .stream()
                .map(mapper::toModel)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public Optional<BatchImport> lookup(long id) {
        log.trace("Importer lookup by id: {}", id);

        return entityManager
                .from(ImportJpa.class)
                .fieldEq("id", id)
                .fieldEq("archived", false)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(mapper::toModel);
    }

    @Override
    public Optional<BatchImport> lookup(String slug) {
        log.trace("Importer lookup by slug: {}", slug);

        return entityManager
                .from(ImportJpa.class)
                .fieldEq("slug", slug)
                .fieldEq("archived", false)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(mapper::toModel);
    }

    @Override
    public ResultPage<BatchImport> lookup(FilterCommand filter) {
        log.trace("Importer lookup by filter: {}", filter);

        return entityManager
                .from(ImportJpa.class)
                .fieldEq("archived", false)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .orderBy("created", false)
                .skip(filter.page() * filter.pageSize())
                .limit(filter.pageSize())
                .paged()
                .map(mapper::toModel);
    }

    @Override
    public String typeOf() {
        return TransactionLinkType.IMPORT.name();
    }
}
