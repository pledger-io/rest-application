package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.importer.entity.ImportConfig;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
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

        var hql = """
                select b from ImportConfig b
                where b.name = :name
                    and b.user.username = :username
                """;

        return entityManager.<ImportConfig>blocking()
                .hql(hql)
                .set("name", name)
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public Sequence<BatchImportConfig> lookup() {
        log.trace("CSVConfiguration listing");

        var hql = """
                select b from ImportConfig b
                where b.user.username = :username""";

        return entityManager.<ImportConfig>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .sequence()
                .map(this::convert);
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
