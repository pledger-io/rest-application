package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.importer.entity.ImportConfig;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.ImportConfigurationProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReadOnly
@RequiresJpa
@Singleton
public class ImportConfigurationProviderJpa implements ImportConfigurationProvider {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public ImportConfigurationProviderJpa(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public Optional<BatchImportConfig> lookup(String name) {
        log.trace("Import configuration lookup by name {}", name);

        return entityManager.from(ImportConfig.class)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(this::convert);
    }

    @Override
    public Sequence<BatchImportConfig> lookup() {
        log.trace("CSVConfiguration listing");

        return entityManager.from(ImportConfig.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .stream()
                .map(this::convert)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    private BatchImportConfig convert(ImportConfig source) {
        if (source == null) {
            return null;
        }

        return BatchImportConfig.builder()
                .id(source.getId())
                .name(source.getName())
                .type(source.getType())
                .fileCode(source.getFileCode())
                .user(UserAccount.builder()
                        .id(source.getUser().getId())
                        .username(new UserIdentifier(source.getUser().getUsername()))
                        .build())
                .build();
    }

}
