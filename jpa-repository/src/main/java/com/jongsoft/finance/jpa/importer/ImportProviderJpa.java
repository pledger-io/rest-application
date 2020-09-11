package com.jongsoft.finance.jpa.importer;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.importer.ImportProvider;
import com.jongsoft.finance.jpa.FilterDelegate;
import com.jongsoft.finance.jpa.core.DataProviderJpa;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.control.Optional;

import io.micronaut.data.model.Sort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ImportProviderJpa extends DataProviderJpa<BatchImport, ImportJpa> implements ImportProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public ImportProviderJpa(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        super(entityManager, ImportJpa.class);
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<BatchImport> lookup(String slug) {
        log.trace("Importer lookup by slug: {}", slug);

        var hql = """
                select b from ImportJpa b
                where b.slug = :slug
                    and b.user.username = :username""";

        var query = entityManager.createQuery(hql);
        query.setParameter("slug", slug);
        query.setParameter("username", authenticationFacade.authenticated());

        return API.Option(convert(singleValue(query)));
    }

    @Override
    public ResultPage<BatchImport> lookup(FilterCommand filter) {
        log.trace("Importer lookup by filter: {}", filter);

        var offset = filter.page() * filter.pageSize();
        return queryPage(new FilterDelegate<>() {
            @Override
            public String generateHql() {
                return "from ImportJpa a where a.user.username = :username";
            }

            @Override
            public FilterDelegate user(String username) {
                return this;
            }

            @Override
            public FilterDelegate prepareQuery(Query query) {
                query.setParameter("username", authenticationFacade.authenticated());
                return this;
            }

            @Override
            public Sort sort() {
                return Sort.of(Sort.Order.desc("a.created"));
            }
        }, API.Option(offset), API.Option(filter.pageSize()));
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
