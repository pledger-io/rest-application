package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReadOnly
@Singleton
@RequiresJpa
public class ImportProviderJpa implements ImportProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    @Inject
    public ImportProviderJpa(AuthenticationFacade authenticationFacade, ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<BatchImport> lookup(String slug) {
        log.trace("Importer lookup by slug: {}", slug);

        return entityManager.from(ImportJpa.class)
                .fieldEq("slug", slug)
                .fieldEq("archived", false)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(this::convert);
    }

    @Override
    public ResultPage<BatchImport> lookup(FilterCommand filter) {
        log.trace("Importer lookup by filter: {}", filter);

        return entityManager.from(ImportJpa.class)
                .fieldEq("archived", false)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .orderBy("created", false)
                .skip(filter.page() * filter.pageSize())
                .limit(filter.pageSize())
                .paged()
                .map(this::convert);
    }

    protected BatchImport convert(ImportJpa source) {
        if (source == null) {
            return null;
        }

        return BatchImport.builder()
                .id(source.getId())
                .created(source.getCreated())
                .fileCode(source.getFileCode())
                .slug(source.getSlug())
                .finished(source.getFinished())
                .config(BatchImportConfig.builder()
                        .type(source.getConfig().getType())
                        .name(source.getConfig().getName())
                        .fileCode(source.getConfig().getFileCode())
                        .build())
                .build();
    }

}
