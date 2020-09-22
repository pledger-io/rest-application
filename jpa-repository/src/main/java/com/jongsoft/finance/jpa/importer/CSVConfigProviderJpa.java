package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.importer.CSVConfigProvider;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.importer.entity.CSVImportConfig;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class CSVConfigProviderJpa extends RepositoryJpa implements CSVConfigProvider {

    private final EntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public CSVConfigProviderJpa(EntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public Optional<BatchImportConfig> lookup(String name) {
        log.trace("CSVConfiguration lookup by name {}", name);

        var hql = """
                select b from CSVImportConfig b 
                where b.name = :name
                    and b.user.username = :username
                """;

        var query = entityManager.createQuery(hql);
        query.setParameter("name", name);
        query.setParameter("username", authenticationFacade.authenticated());

        return API.Option(convert(singleValue(query)));
    }

    @Override
    public Sequence<BatchImportConfig> lookup() {
        log.trace("CSVConfiguration listing");

        var hql = """
                select b from CSVImportConfig b
                where b.user.username = :username""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());

        return this.<CSVImportConfig>multiValue(query)
                .map(this::convert);
    }

    private BatchImportConfig convert(CSVImportConfig source) {
        if (source == null) {
            return null;
        }

        return BatchImportConfig.builder()
                .id(source.getId())
                .name(source.getName())
                .fileCode(source.getFileCode())
                .user(UserAccount.builder()
                        .id(source.getUser().getId())
                        .username(source.getUser().getUsername())
                        .build())
                .build();
    }

}
