package com.jongsoft.finance.exporter.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.exporter.adapter.api.ImportConfigurationProvider;
import com.jongsoft.finance.exporter.domain.jpa.entity.ImportConfig;
import com.jongsoft.finance.exporter.domain.jpa.mapper.ImportConfigMapper;
import com.jongsoft.finance.exporter.domain.model.BatchImportConfig;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ReadOnly
@Singleton
class ImportConfigurationProviderJpa implements ImportConfigurationProvider {

    private final Logger log = LoggerFactory.getLogger(ImportConfigurationProviderJpa.class);
    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;
    private final ImportConfigMapper mapper;

    public ImportConfigurationProviderJpa(
            ReactiveEntityManager entityManager,
            AuthenticationFacade authenticationFacade,
            ImportConfigMapper mapper) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
        this.mapper = mapper;
    }

    @Override
    public Optional<BatchImportConfig> lookup(String name) {
        log.trace("Import configuration lookup by name {}", name);

        return entityManager
                .from(ImportConfig.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(mapper::toModel);
    }

    @Override
    public Sequence<BatchImportConfig> lookup() {
        log.trace("CSVConfiguration listing");

        return entityManager
                .from(ImportConfig.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .stream()
                .map(mapper::toModel)
                .collect(ReactiveEntityManager.sequenceCollector());
    }
}
