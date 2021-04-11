package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.data.model.Sort;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class ImportProviderJpa implements ImportProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    public ImportProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Maybe<BatchImport> lookup(String slug) {
        log.trace("Importer lookup by slug: {}", slug);

        var hql = """
                select b from ImportJpa b
                where b.slug = :slug
                    and b.archived = false
                    and b.user.username = :username""";

        return entityManager.<ImportJpa>reactive()
                .hql(hql)
                .set("slug", slug)
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .map(this::convert);
    }

    @Override
    public ResultPage<BatchImport> lookup(FilterCommand filter) {
        log.trace("Importer lookup by filter: {}", filter);

        return entityManager.<ImportJpa>blocking()
                .hql("from ImportJpa a where a.user.username = :username and a.archived = false")
                .set("username", authenticationFacade.authenticated())
                .limit(filter.pageSize())
                .offset(filter.page() * filter.pageSize())
                .sort(Sort.of(Sort.Order.desc("a.created")))
                .page()
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
                        .name(source.getConfig().getName())
                        .fileCode(source.getConfig().getFileCode())
                        .build())
                .build();
    }

}
